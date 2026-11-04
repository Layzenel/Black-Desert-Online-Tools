package com.layzen.bdotools.feature.slots.data

/**
 * Мост к File System Access API. Вся работа с браузером — в JS ниже, Kotlin получает только строки:
 * `OK:<данные>` или `ERR:<код>`. Картинки передаются в base64, тексты — как есть.
 *
 * Папка `Black Desert` хранится в той же записи IndexedDB, что и у страницы портретов,
 * поэтому выбрать её достаточно один раз на весь сайт.
 */
internal const val SLOTS_BRIDGE_SOURCE = """(() => {
    if (globalThis.__bdoSlots) return;
    const DB = 'bdo-tools', STORE = 'handles', KEY = 'black-desert';
    const CACHE = 'UserCache', GAME_FILE = 'gamevariable.xml', TOOLS = 'BDO Tools';

    const idb = (mode, action) => new Promise((resolve, reject) => {
        const open = indexedDB.open(DB, 1);
        open.onupgradeneeded = () => open.result.createObjectStore(STORE);
        open.onerror = () => reject(open.error);
        open.onsuccess = () => {
            const tx = open.result.transaction(STORE, mode);
            const request = action(tx.objectStore(STORE));
            tx.oncomplete = () => resolve(request ? request.result : undefined);
            tx.onerror = () => reject(tx.error);
        };
    });
    const savedRoot = () => idb('readonly', s => s.get(KEY));
    const child = async (dir, name, create) => { try { return await dir.getDirectoryHandle(name, { create: !!create }); } catch (e) { return null; } };
    const gameDir = async dir => {
        if (await child(dir, CACHE)) return dir;
        const nested = await child(dir, 'Black Desert');
        return nested && await child(nested, CACHE) ? nested : null;
    };
    // Black Desert\BDO Tools\slots\<sub>
    const tools = async (sub, create) => {
        const app = await child(await savedRoot(), TOOLS, create);
        const slots = app && await child(app, 'slots', create);
        return sub ? slots && await child(slots, sub, create) : slots;
    };
    const accountDir = async account => (await (await savedRoot()).getDirectoryHandle(CACHE)).getDirectoryHandle(account);
    const gameFile = async account => (await accountDir(account)).getFileHandle(GAME_FILE);
    // path — «11/<персонаж>/<имя файла>» от папки аккаунта
    const characterFile = async (account, path) => {
        const [server, id, name] = path.split('/');
        return (await (await (await accountDir(account)).getDirectoryHandle(server)).getDirectoryHandle(id)).getFileHandle(name);
    };
    // ignoreBOM: метка порядка байтов, если она есть, остаётся в тексте и вернётся в файл при записи
    const readText = async file => new TextDecoder('utf-8', { ignoreBOM: true }).decode(await file.arrayBuffer());
    const writeHandle = async (handle, data) => {
        const writable = await handle.createWritable();
        await writable.write(data);
        await writable.close();
    };
    const writeTo = async (dir, name, data) => writeHandle(await dir.getFileHandle(name, { create: true }), data);
    const toBase64 = blob => new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(String(reader.result).substring(String(reader.result).indexOf(',') + 1));
        reader.onerror = () => reject(reader.error);
        reader.readAsDataURL(blob);
    });
    const fromBase64 = text => Uint8Array.from(atob(text), c => c.charCodeAt(0));
    const stamp = () => {
        const d = new Date(), p = n => String(n).padStart(2, '0');
        return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + '_' +
            p(d.getHours()) + '-' + p(d.getMinutes()) + '-' + p(d.getSeconds());
    };
    const pickFile = accept => new Promise(resolve => {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = accept;
        input.onchange = () => resolve(input.files && input.files[0] ? input.files[0] : null);
        input.oncancel = () => resolve(null);
        input.click();
    });
    const fail = e => 'ERR:' + (e && e.name === 'NotAllowedError' ? 'PERMISSION' : 'UNKNOWN:' + (e && e.message));

    const api = {
        async status() {
            const root = await savedRoot();
            if (!root) return 'OK:none';
            const permission = await root.queryPermission({ mode: 'readwrite' });
            return 'OK:' + (permission === 'granted' ? 'granted' : 'prompt') + ':' + root.name;
        },
        async request() {
            const root = await savedRoot();
            if (!root) return 'ERR:NOT_GAME_FOLDER';
            const permission = await root.requestPermission({ mode: 'readwrite' });
            return permission === 'granted' ? 'OK:' + root.name : 'ERR:PERMISSION';
        },
        async pick() {
            let picked;
            try {
                picked = await showDirectoryPicker({ id: 'bdo-black-desert', mode: 'readwrite', startIn: 'documents' });
            } catch (e) {
                return 'ERR:CANCELLED';
            }
            const root = await gameDir(picked);
            if (!root) return 'ERR:NOT_GAME_FOLDER';
            await idb('readwrite', s => s.put(root, KEY));
            return 'OK:' + root.name;
        },
        // Аккаунты по свежести файла: игра пишет в папку того аккаунта, под которым играли последним
        async accounts() {
            const cache = await child(await savedRoot(), CACHE);
            if (!cache) return 'OK:';
            const found = [];
            for await (const [name, handle] of cache.entries()) {
                if (handle.kind !== 'directory') continue;
                try {
                    found.push([name, (await (await handle.getFileHandle(GAME_FILE)).getFile()).lastModified]);
                } catch (e) { /* в папке нет файла настроек */ }
            }
            return 'OK:' + found.sort((a, b) => b[1] - a[1]).map(f => f[0]).join('\n');
        },
        async read(account) {
            const file = await (await gameFile(account)).getFile();
            return 'OK:' + file.lastModified + '\n' + await readText(file);
        },
        async write(account, payload) {
            const expected = Number(payload.substring(0, payload.indexOf('\n')));
            const text = payload.substring(payload.indexOf('\n') + 1);
            const handle = await gameFile(account);
            const current = await handle.getFile();
            if (current.lastModified !== expected) return 'ERR:FILE_CHANGED';
            const backups = await tools('backups', true);
            const backupName = 'gamevariable_' + account + '_' + stamp() + '.xml';
            await writeTo(backups, backupName, current);
            await writeHandle(handle, new TextEncoder().encode(text));
            return 'OK:' + TOOLS + '\\slots\\backups\\' + backupName;
        },
        // Настройки персонажей: UserCache\<аккаунт>\11\<персонаж>\gamevariable.xml (регистр имени файла бывает разный)
        async characters(account) {
            const servers = await child(await accountDir(account), '11');
            const paths = [];
            if (servers) for await (const [id, dir] of servers.entries()) {
                if (dir.kind !== 'directory') continue;
                for await (const [name, file] of dir.entries()) {
                    if (file.kind === 'file' && name.toLowerCase() === GAME_FILE) paths.push('11/' + id + '/' + name);
                }
            }
            return 'OK:' + paths.join('\n');
        },
        characterRead: async (account, path) => 'OK:' + await readText(await (await characterFile(account, path)).getFile()),
        async characterWrite(account, payload) {
            const path = payload.substring(0, payload.indexOf('\n'));
            const handle = await characterFile(account, path);
            const backupName = 'gamevariable_' + account + '_' + path.split('/')[1] + '_' + stamp() + '.xml';
            await writeTo(await tools('backups', true), backupName, await handle.getFile());
            await writeHandle(handle, new TextEncoder().encode(payload.substring(payload.indexOf('\n') + 1)));
            return 'OK:';
        },
        async presets() {
            const dir = await tools('presets', false);
            const names = [];
            if (dir) for await (const [name, handle] of dir.entries()) {
                if (handle.kind === 'file' && name.toLowerCase().endsWith('.json')) names.push(name);
            }
            return 'OK:' + names.join('\n');
        },
        presetRead: async name => 'OK:' + await readText(await (await (await tools('presets', false)).getFileHandle(name)).getFile()),
        async presetWrite(name, text) {
            await writeTo(await tools('presets', true), name, new TextEncoder().encode(text));
            return 'OK:';
        },
        async presetDelete(name) {
            const dir = await tools('presets', false);
            if (dir) {
                try { await dir.removeEntry(name); } catch (e) { /* файла уже нет */ }
            }
            return 'OK:';
        },
        async backgroundRead() {
            const dir = await tools('', false);
            try {
                return 'OK:1' + await toBase64(await (await dir.getFileHandle('background')).getFile());
            } catch (e) {
                return 'OK:0';
            }
        },
        async backgroundWrite(base64) {
            const dir = await tools('', true);
            if (base64) await writeTo(dir, 'background', fromBase64(base64));
            else try { await dir.removeEntry('background'); } catch (e) { /* подложки уже нет */ }
            return 'OK:';
        },
        async pickImage() {
            const file = await pickFile('image/*');
            return file ? 'OK:' + await toBase64(file) : 'ERR:CANCELLED';
        },
        async pickLayout() {
            const file = await pickFile('.json,application/json');
            return file ? 'OK:' + await readText(file) : 'ERR:CANCELLED';
        },
        download(name, text) {
            const link = document.createElement('a');
            link.href = URL.createObjectURL(new Blob([text], { type: 'application/json' }));
            link.download = name;
            link.click();
            setTimeout(() => URL.revokeObjectURL(link.href), 1000);
            return 'OK:';
        },
    };
    globalThis.__bdoSlots = {};
    for (const [name, action] of Object.entries(api)) {
        globalThis.__bdoSlots[name] = (a, b) => Promise.resolve().then(() => action(a, b)).catch(fail);
    }
})();"""
