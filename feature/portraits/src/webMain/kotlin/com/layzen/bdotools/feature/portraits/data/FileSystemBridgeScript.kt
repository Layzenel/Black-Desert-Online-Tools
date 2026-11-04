@file:OptIn(ExperimentalWasmJsInterop::class)

package com.layzen.bdotools.feature.portraits.data

import kotlin.js.Promise
import kotlinx.coroutines.await

/**
 * Мост к File System Access API и загрузке картинок. Вся работа с браузером — в JS ниже,
 * Kotlin получает только строки: `OK:<данные>` или `ERR:<код>`. Файлы передаются в base64.
 *
 * Выбранная папка хранится в IndexedDB, поэтому после перезагрузки страницы её не нужно выбирать
 * заново — достаточно подтвердить разрешение.
 */
internal const val BRIDGE_SOURCE = """(() => {
    if (globalThis.__bdoPortraits) return;
    const DB = 'bdo-tools', STORE = 'handles', KEY = 'black-desert';
    const FACES = 'FaceTexture', BACKUP = 'FaceTexture_backup', CACHE = 'UserCache', TOOLS = 'BDO Tools';
    const FAVORITES = 'Favorite FaceTexture';
    const SPLIT = '\n<!--bdo-split-->\n';

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
    const child = async (dir, name) => { try { return await dir.getDirectoryHandle(name); } catch (e) { return null; } };
    const gameDir = async dir => {
        if (await child(dir, FACES)) return dir;
        const nested = await child(dir, 'Black Desert');
        return nested && await child(nested, FACES) ? nested : null;
    };
    const faces = async () => (await savedRoot()).getDirectoryHandle(FACES);
    // Black Desert\BDO Tools\portraits — чистые портреты и файлы украшений
    const tools = async create => {
        try {
            const app = await (await savedRoot()).getDirectoryHandle(TOOLS, { create });
            return await app.getDirectoryHandle('portraits', { create });
        } catch (e) {
            return null;
        }
    };
    // Новая папка с датой и временем: две папки в одну секунду не должны сливаться в одну
    const freshFolder = async parent => {
        let folder = stamp();
        for (let n = 2; await child(parent, folder); n++) folder = stamp() + '_' + n;
        return folder;
    };
    // Папки резервных копий или избранного — строки «папка<Tab>портрет.bmp,…»; папки может ещё не быть
    const listFolders = async dir => {
        if (!dir) return 'OK:';
        const lines = [];
        for await (const [folder, handle] of dir.entries()) {
            if (handle.kind !== 'directory') continue;
            const files = [];
            for await (const [name, file] of handle.entries()) {
                if (file.kind === 'file' && name.toLowerCase().endsWith('.bmp')) files.push(name);
            }
            lines.push(folder + '\t' + files.join(','));
        }
        return 'OK:' + lines.join('\n');
    };
    const favoriteFolder = async folder => child(await child(await savedRoot(), FAVORITES), folder);
    // Файлы в папке тулзы; папки может ещё не быть
    const listFiles = async dir => {
        const names = [];
        if (dir) for await (const [name, handle] of dir.entries()) if (handle.kind === 'file') names.push(name);
        return 'OK:' + names.join('\n');
    };
    const readFile = async (dir, name) => {
        if (!dir) return 'OK:0';
        try {
            return 'OK:1' + await toBase64(await (await dir.getFileHandle(name)).getFile());
        } catch (e) {
            return 'OK:0';
        }
    };
    const writeFile = async (dir, name, base64) => {
        const writable = await (await dir.getFileHandle(name, { create: true })).createWritable();
        await writable.write(fromBase64(base64));
        await writable.close();
        return 'OK:';
    };
    const toBase64 = blob => new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(String(reader.result).substring(String(reader.result).indexOf(',') + 1));
        reader.onerror = () => reject(reader.error);
        reader.readAsDataURL(blob);
    });
    const fromBase64 = text => {
        const raw = atob(text);
        const bytes = new Uint8Array(raw.length);
        for (let i = 0; i < raw.length; i++) bytes[i] = raw.charCodeAt(i);
        return bytes;
    };
    const stamp = () => {
        const d = new Date(), p = n => String(n).padStart(2, '0');
        return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + '_' +
            p(d.getHours()) + '-' + p(d.getMinutes()) + '-' + p(d.getSeconds());
    };
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
        async list() {
            const names = [];
            for await (const [name, handle] of (await faces()).entries()) {
                if (handle.kind === 'file' && name.toLowerCase().endsWith('.bmp')) names.push(name);
            }
            return 'OK:' + names.join('\n');
        },
        async read(name) {
            const file = await (await (await faces()).getFileHandle(name)).getFile();
            return 'OK:' + await toBase64(file);
        },
        async order() {
            const cache = await child(await savedRoot(), CACHE);
            if (!cache) return 'OK:';
            const parts = [];
            const collect = async dir => {
                for await (const [name, handle] of dir.entries()) {
                    if (handle.kind !== 'file' || name.toLowerCase() !== 'gamevariable.xml') continue;
                    const text = await (await handle.getFile()).text();
                    const start = text.indexOf('<CharacterOrderList Version');
                    if (start < 0) continue;
                    const end = text.indexOf('</CharacterOrderList>', start);
                    parts.push(text.substring(start, end < 0 ? text.length : end));
                }
            };
            await collect(cache);
            for await (const [, handle] of cache.entries()) {
                if (handle.kind === 'directory') await collect(handle);
            }
            return 'OK:' + parts.join(SPLIT);
        },
        async backup(names) {
            const root = await savedRoot();
            const source = await root.getDirectoryHandle(FACES);
            const backups = await root.getDirectoryHandle(BACKUP, { create: true });
            const folder = await freshFolder(backups);
            const target = await backups.getDirectoryHandle(folder, { create: true });
            for (const name of names.split('\n')) {
                const file = await (await source.getFileHandle(name)).getFile();
                const writable = await (await target.getFileHandle(name, { create: true })).createWritable();
                await writable.write(file);
                await writable.close();
            }
            return 'OK:' + BACKUP + '\\' + folder;
        },
        backups: async () => listFolders(await child(await savedRoot(), BACKUP)),
        async backupDelete(folder) {
            const dir = await child(await savedRoot(), BACKUP);
            if (dir) {
                try { await dir.removeEntry(folder, { recursive: true }); } catch (e) { if (e.name !== 'NotFoundError') throw e; }
            }
            return 'OK:';
        },
        async backupRead(folder, name) {
            const dir = await (await (await savedRoot()).getDirectoryHandle(BACKUP)).getDirectoryHandle(folder);
            return 'OK:' + await toBase64(await (await dir.getFileHandle(name)).getFile());
        },
        async write(name, base64) {
            const handle = await (await faces()).getFileHandle(name, { create: true });
            const writable = await handle.createWritable();
            await writable.write(fromBase64(base64));
            await writable.close();
            return 'OK:';
        },
        toolList: async () => listFiles(await tools(false)),
        toolRead: async name => readFile(await tools(false), name),
        toolWrite: async (name, base64) => writeFile(await tools(true), name, base64),
        async toolDelete(name) {
            const dir = await tools(false);
            if (dir) {
                try { await dir.removeEntry(name); } catch (e) { /* файла уже нет */ }
            }
            return 'OK:';
        },
        // Избранное: Black Desert\Favorite FaceTexture\<дата-время>\ — набор всех портретов
        async favoriteCreate() {
            const dir = await (await savedRoot()).getDirectoryHandle(FAVORITES, { create: true });
            const folder = await freshFolder(dir);
            await dir.getDirectoryHandle(folder, { create: true });
            return 'OK:' + folder;
        },
        favorites: async () => listFolders(await child(await savedRoot(), FAVORITES)),
        favoriteRead: async (folder, name) => readFile(await favoriteFolder(folder), name),
        // мост передаёт два аргумента, поэтому папка и имя файла приходят одной строкой «папка/имя»
        favoriteWrite: async (path, base64) =>
            writeFile(await favoriteFolder(path.substring(0, path.indexOf('/'))), path.substring(path.indexOf('/') + 1), base64),
        async favoriteDelete(folder) {
            const dir = await child(await savedRoot(), FAVORITES);
            if (dir) {
                try { await dir.removeEntry(folder, { recursive: true }); } catch (e) { if (e.name !== 'NotFoundError') throw e; }
            }
            return 'OK:';
        },
        pickImage() {
            return new Promise(resolve => {
                const input = document.createElement('input');
                input.type = 'file';
                input.accept = 'image/*';
                input.onchange = async () => {
                    const file = input.files && input.files[0];
                    resolve(file ? 'OK:' + await toBase64(file) : 'ERR:CANCELLED');
                };
                input.oncancel = () => resolve('ERR:CANCELLED');
                input.click();
            });
        },
        async download(url) {
            let response;
            try {
                response = await fetch(url);
            } catch (e) {
                return 'ERR:BLOCKED';
            }
            if (!response.ok) return 'ERR:NETWORK';
            const blob = await response.blob();
            if (!blob.type.startsWith('image/')) return 'ERR:NOT_IMAGE';
            return 'OK:' + await toBase64(blob);
        },
    };
    globalThis.__bdoPortraits = {};
    for (const [name, action] of Object.entries(api)) {
        globalThis.__bdoPortraits[name] = (a, b) => Promise.resolve().then(() => action(a, b)).catch(fail);
    }
})();"""
