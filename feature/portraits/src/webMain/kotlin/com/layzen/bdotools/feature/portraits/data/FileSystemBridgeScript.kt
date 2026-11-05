package com.layzen.bdotools.feature.portraits.data

/**
 * Методы портретов для общего моста к папке `Black Desert` (`FolderBridge` в `platform/core`):
 * портреты, резервные копии, файлы украшений, избранное и загрузка картинок.
 * Kotlin получает только строки: `OK:<данные>` или `ERR:<код>`. Файлы передаются в base64.
 */
internal const val BRIDGE_SOURCE = """({ savedRoot, child, toBase64, fromBase64, stamp, pickFile }) => {
    const FACES = 'FaceTexture', BACKUP = 'FaceTexture_backup', TOOLS = 'BDO Tools';
    const FAVORITES = 'Favorite FaceTexture';
    const USER_CACHE = 'UserCache', LOBBY = 'gameVariable_lobby.xml';

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
    const backupFolder = async folder => child(await child(await savedRoot(), BACKUP), folder);
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

    return {
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
        // Резервная копия: Black Desert\FaceTexture_backup\<дата-время>\ — чистые портреты и украшения
        async backupCreate() {
            const dir = await (await savedRoot()).getDirectoryHandle(BACKUP, { create: true });
            const folder = await freshFolder(dir);
            await dir.getDirectoryHandle(folder, { create: true });
            return 'OK:' + folder;
        },
        backups: async () => listFolders(await child(await savedRoot(), BACKUP)),
        async backupDelete(folder) {
            const dir = await child(await savedRoot(), BACKUP);
            if (dir) {
                try { await dir.removeEntry(folder, { recursive: true }); } catch (e) { if (e.name !== 'NotFoundError') throw e; }
            }
            return 'OK:';
        },
        backupRead: async (folder, name) => readFile(await backupFolder(folder), name),
        // мост передаёт два аргумента, поэтому папка и имя файла приходят одной строкой «папка/имя»
        backupWrite: async (path, base64) =>
            writeFile(await backupFolder(path.substring(0, path.indexOf('/'))), path.substring(path.indexOf('/') + 1), base64),
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
        // Заметки персонажей: Black Desert\UserCache\<аккаунт>\gameVariable_lobby.xml
        lobbyRead: async account => readFile(await child(await child(await savedRoot(), USER_CACHE), account), LOBBY),
        async lobbyWrite(account, base64) {
            const dir = await (await (await savedRoot()).getDirectoryHandle(USER_CACHE)).getDirectoryHandle(account);
            return writeFile(dir, LOBBY, base64);
        },
        // Копия перед записью: Black Desert\BDO Tools\lobby_backup\<аккаунт>\<дата-время>.xml
        async lobbyBackup(account, base64) {
            const app = await (await savedRoot()).getDirectoryHandle(TOOLS, { create: true });
            const backups = await app.getDirectoryHandle('lobby_backup', { create: true });
            const dir = await backups.getDirectoryHandle(account, { create: true });
            const exists = async name => { try { await dir.getFileHandle(name); return true; } catch (e) { return false; } };
            let name = stamp() + '.xml';
            for (let n = 2; await exists(name); n++) name = stamp() + '_' + n + '.xml';
            await writeFile(dir, name, base64);
            return 'OK:' + TOOLS + '\\lobby_backup\\' + account + '\\' + name;
        },
        async pickImage() {
            const file = await pickFile('image/*');
            return file ? 'OK:' + await toBase64(file) : 'ERR:CANCELLED';
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
}"""
