package com.layzen.bdotools.feature.uieditor.data

/**
 * Методы слотов для общего моста к папке `Black Desert` (`FolderBridge` в `platform/core`):
 * `gamevariable.xml` аккаунтов, копии, пресеты и подложка в папке аккаунта `BDO Tools\uiEditor\<аккаунт>\`,
 * файлы с устройства.
 * Kotlin получает только строки: `OK:<данные>` или `ERR:<код>`. Картинки передаются в base64, тексты — как есть,
 * файлы игры — символ на байт (Latin-1), чтобы запись не меняла байты, которых тулза не трогала.
 */
internal const val SLOTS_BRIDGE_SOURCE = """({ savedRoot, child, toBase64, fromBase64, stamp, pickFile }) => {
    const CACHE = 'UserCache', GAME_FILE = 'gamevariable.xml', TOOLS = 'BDO Tools', TOOL = 'uiEditor';
    const BACKUPS = 'backups', PRESETS = 'presets', BACKGROUND = 'background', BACKGROUND_FILE = 'image';
    // Папка тулзы в прошлых версиях
    const LEGACY_TOOL = 'slots';

    // Black Desert\BDO Tools\uiEditor\<аккаунт>\<sub>; без create папки может не быть — тогда null
    const area = async (account, sub, create) => {
        let dir = await child(await savedRoot(), TOOLS, create);
        for (const part of [TOOL, account, sub]) {
            if (!dir) return null;
            dir = await child(dir, part, create);
        }
        return dir;
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
    // Файлы игры — символ на байт (Latin-1): в них смешаны UTF-8 и Windows-1251, через UTF-8 кириллица портится.
    // TextDecoder('latin1') не подходит — браузеры понимают его как windows-1252, где 0x80–0x9F не байт в байт.
    const readGameText = async file => {
        const bytes = new Uint8Array(await file.arrayBuffer());
        let text = '';
        for (let i = 0; i < bytes.length; i += 0x8000) text += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));
        return text;
    };
    const gameBytes = text => Uint8Array.from(text, c => c.charCodeAt(0));
    const writeHandle = async (handle, data) => {
        const writable = await handle.createWritable();
        await writable.write(data);
        await writable.close();
    };
    const writeTo = async (dir, name, data) => writeHandle(await dir.getFileHandle(name, { create: true }), data);
    const hasFile = async (dir, name) => { try { await dir.getFileHandle(name); return true; } catch (e) { return false; } };

    // Перенос данных прошлых версий из BDO Tools\slots. Перемещать File System Access API не умеет — копирование
    // и удаление; совпавший файл не перезаписывается и остаётся на старом месте
    const isAccount = name => /^\d+$/.test(name);
    const entriesOf = async dir => {
        const list = [];
        if (dir) for await (const entry of dir.entries()) list.push(entry);
        return list;
    };
    const copyTree = async (from, to) => {
        for (const [name, entry] of await entriesOf(from)) {
            if (entry.kind === 'directory') await copyTree(entry, await to.getDirectoryHandle(name, { create: true }));
            else await writeTo(to, name, await entry.getFile());
        }
    };
    const moveIfFree = async (parent, name, entry, to, newName = name) => {
        if (await hasFile(to, newName) || await child(to, newName)) return;
        if (entry.kind === 'directory') await copyTree(entry, await to.getDirectoryHandle(newName, { create: true }));
        else await writeTo(to, newName, await entry.getFile());
        await parent.removeEntry(name, { recursive: true });
    };
    const moveContents = async (parent, name, from, to) => {
        for (const [inner, entry] of await entriesOf(from)) await moveIfFree(from, inner, entry, to);
        await removeIfEmpty(parent, name);
    };
    const removeIfEmpty = async (parent, name) => {
        const dir = parent && await child(parent, name);
        if (dir && !(await entriesOf(dir)).length) await parent.removeEntry(name);
    };

    return {
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
            return 'OK:' + file.lastModified + '\n' + await readGameText(file);
        },
        // Копия перед записью: <аккаунт>\backups\gamevariable_<аккаунт>_<дата-время>.xml
        async write(account, payload) {
            const expected = Number(payload.substring(0, payload.indexOf('\n')));
            const text = payload.substring(payload.indexOf('\n') + 1);
            const handle = await gameFile(account);
            const current = await handle.getFile();
            if (current.lastModified !== expected) return 'ERR:FILE_CHANGED';
            const backupName = 'gamevariable_' + account + '_' + stamp() + '.xml';
            await writeTo(await area(account, BACKUPS, true), backupName, current);
            await writeHandle(handle, gameBytes(text));
            return 'OK:' + [TOOLS, TOOL, account, BACKUPS, backupName].join('\\');
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
        characterRead: async (account, path) => 'OK:' + await readGameText(await (await characterFile(account, path)).getFile()),
        async characterWrite(account, payload) {
            const path = payload.substring(0, payload.indexOf('\n'));
            const handle = await characterFile(account, path);
            const backupName = 'gamevariable_' + account + '_' + path.split('/')[1] + '_' + stamp() + '.xml';
            await writeTo(await area(account, BACKUPS, true), backupName, await handle.getFile());
            await writeHandle(handle, gameBytes(payload.substring(payload.indexOf('\n') + 1)));
            return 'OK:';
        },
        // Пресеты аккаунта: <аккаунт>\presets\*.json
        async presets(account) {
            const names = [];
            for (const [name, handle] of await entriesOf(await area(account, PRESETS, false))) {
                if (handle.kind === 'file' && name.toLowerCase().endsWith('.json')) names.push(name);
            }
            return 'OK:' + names.join('\n');
        },
        presetRead: async (account, name) =>
            'OK:' + await readText(await (await (await area(account, PRESETS, false)).getFileHandle(name)).getFile()),
        // мост передаёт два аргумента, поэтому аккаунт и имя файла приходят одной строкой «аккаунт/файл»
        async presetWrite(path, text) {
            const account = path.substring(0, path.indexOf('/'));
            const name = path.substring(path.indexOf('/') + 1);
            await writeTo(await area(account, PRESETS, true), name, new TextEncoder().encode(text));
            return 'OK:';
        },
        async presetDelete(account, name) {
            const dir = await area(account, PRESETS, false);
            if (dir) {
                try { await dir.removeEntry(name); } catch (e) { /* файла уже нет */ }
            }
            return 'OK:';
        },
        // Подложка аккаунта: файл <аккаунт>\background\image
        async backgroundRead(account) {
            try {
                return 'OK:1' + await toBase64(await (await (await area(account, BACKGROUND, false)).getFileHandle(BACKGROUND_FILE)).getFile());
            } catch (e) {
                return 'OK:0';
            }
        },
        async backgroundWrite(account, base64) {
            const dir = await area(account, BACKGROUND, true);
            if (base64) await writeTo(dir, BACKGROUND_FILE, fromBase64(base64));
            else try { await dir.removeEntry(BACKGROUND_FILE); } catch (e) { /* подложки уже нет */ }
            return 'OK:';
        },
        // Данные прошлых версий из BDO Tools\slots: где аккаунт известен (папка или имя копии с его номером) — ему,
        // остальное (пресеты прямо в presets, файл background) — аккаунту, который открыли первым
        async adoptLoose(account) {
            const tools = await child(await savedRoot(), TOOLS);
            const legacy = tools && await child(tools, LEGACY_TOOL);
            if (!legacy) return 'OK:';
            const presets = await child(legacy, PRESETS);
            for (const [name, entry] of await entriesOf(presets)) {
                if (entry.kind === 'directory' && isAccount(name)) await moveContents(presets, name, entry, await area(name, PRESETS, true));
                else await moveIfFree(presets, name, entry, await area(account, PRESETS, true));
            }
            if (await hasFile(legacy, BACKGROUND)) {
                await moveIfFree(legacy, BACKGROUND, await legacy.getFileHandle(BACKGROUND), await area(account, BACKGROUND, true), BACKGROUND_FILE);
            } else {
                const backgrounds = await child(legacy, BACKGROUND);
                for (const [name, entry] of await entriesOf(backgrounds)) {
                    if (entry.kind === 'file' && isAccount(name)) await moveIfFree(backgrounds, name, entry, await area(name, BACKGROUND, true), BACKGROUND_FILE);
                }
            }
            const backups = await child(legacy, BACKUPS);
            for (const [name, entry] of await entriesOf(backups)) {
                const owner = entry.kind === 'directory' ? (isAccount(name) ? name : null) : ((/^gamevariable_(\d+)_/.exec(name) || [])[1] || null);
                if (entry.kind === 'directory' && owner) await moveContents(backups, name, entry, await area(owner, BACKUPS, true));
                else await moveIfFree(backups, name, entry, await area(owner || account, BACKUPS, true));
            }
            for (const name of [PRESETS, BACKGROUND, BACKUPS]) await removeIfEmpty(legacy, name);
            await removeIfEmpty(tools, LEGACY_TOOL);
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
}"""
