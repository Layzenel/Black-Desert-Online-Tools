package com.layzen.bdotools.feature.portraitconstructor.data

/**
 * Методы портретов для общего моста к папке `Black Desert` (`FolderBridge` в `platform/core`):
 * портреты, резервные копии, файлы украшений, избранное и загрузка картинок. Всё своё лежит в папке аккаунта
 * `BDO Tools/portraitConstructor/<аккаунт>/`: `FaceTexture_backup`, `Favorite FaceTexture`, `Current portraits`,
 * `lobby_backup`; пустой аккаунт — без папки аккаунта.
 * Kotlin получает только строки: `OK:<данные>` или `ERR:<код>`. Файлы передаются в base64.
 */
internal const val BRIDGE_SOURCE = """({ savedRoot, child, toBase64, fromBase64, stamp, pickFile }) => {
    const FACES = 'FaceTexture', BACKUP = 'FaceTexture_backup', TOOLS = 'BDO Tools', TOOL = 'portraitConstructor';
    const FAVORITES = 'Favorite FaceTexture', CURRENT = 'Current portraits', LOBBY_BACKUP = 'lobby_backup';
    const USER_CACHE = 'UserCache', LOBBY = 'gameVariable_lobby.xml';
    // Папка тулзы прошлой версии: BDO Tools\portraits с файлами портретов и папками копий и избранного внутри
    const LEGACY_TOOL = 'portraits';

    const faces = async () => (await savedRoot()).getDirectoryHandle(FACES);
    // Black Desert\BDO Tools\portraitConstructor\<аккаунт>\<name>; пустой аккаунт — без его папки.
    // Без create папки может не быть — тогда null
    const area = async (name, account, create) => {
        let dir = await child(await savedRoot(), TOOLS, create);
        for (const part of [TOOL, account, name]) {
            if (!dir) return null;
            if (part) dir = await child(dir, part, create);
        }
        return dir;
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
    // мост передаёт два аргумента, поэтому аккаунт и папка приходят одной строкой «аккаунт/папка»
    const split = path => [path.substring(0, path.indexOf('/')), path.substring(path.indexOf('/') + 1)];
    const areaFolder = async (name, path) => {
        const [account, folder] = split(path);
        const dir = await area(name, account, false);
        return dir && child(dir, folder);
    };
    const areaCreate = async (name, account) => {
        const dir = await area(name, account, true);
        const folder = await freshFolder(dir);
        await dir.getDirectoryHandle(folder, { create: true });
        return 'OK:' + folder;
    };
    const removeFrom = async (dir, name) => {
        if (dir) {
            try { await dir.removeEntry(name, { recursive: true }); } catch (e) { if (e.name !== 'NotFoundError') throw e; }
        }
        return 'OK:';
    };
    // «аккаунт/папка/файл»: папку копии или набора создаёт запись, если её ещё нет
    const areaWrite = async (name, path, base64) => {
        const [account, rest] = split(path);
        const [folder, file] = split(rest);
        const dir = await (await area(name, account, true)).getDirectoryHandle(folder, { create: true });
        return writeFile(dir, file, base64);
    };
    // Файлы в папке; папки может ещё не быть
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

    // Перенос данных прошлых версий. Перемещать File System Access API не умеет — копирование и удаление
    const isAccount = name => /^\d+$/.test(name);
    const hasFile = async (dir, name) => { try { await dir.getFileHandle(name); return true; } catch (e) { return false; } };
    const entriesOf = async dir => {
        const list = [];
        for await (const entry of dir.entries()) list.push(entry);
        return list;
    };
    // Свободное имя в dir: совпавшее получает суффикс _2, _3 — ничего не перезаписывается
    const freeName = async (dir, name) => {
        let free = name;
        for (let n = 2; await child(dir, free) || await hasFile(dir, free); n++) free = name + '_' + n;
        return free;
    };
    const copyFile = async (file, to, name) => {
        const writable = await (await to.getFileHandle(name, { create: true })).createWritable();
        await writable.write(await file.getFile());
        await writable.close();
    };
    const copyTree = async (from, to) => {
        for (const [name, entry] of await entriesOf(from)) {
            if (entry.kind === 'directory') await copyTree(entry, await to.getDirectoryHandle(name, { create: true }));
            else await copyFile(entry, to, name);
        }
    };
    // Запись name из папки parent — в папку to под свободным именем
    const moveEntry = async (parent, name, entry, to) => {
        const free = await freeName(to, name);
        if (entry.kind === 'directory') await copyTree(entry, await to.getDirectoryHandle(free, { create: true }));
        else await copyFile(entry, to, free);
        await parent.removeEntry(name, { recursive: true });
    };
    const removeIfEmpty = async (parent, name) => {
        const dir = parent && await child(parent, name);
        if (dir && !(await entriesOf(dir)).length) await parent.removeEntry(name);
    };
    // Раскладывает parent\name по аккаунтам: папка с номером аккаунта — в target(этот аккаунт), остальное
    // (копии без аккаунта) — в target(account). Опустевшую папку удаляет
    const spreadByAccount = async (parent, name, account, target) => {
        const old = parent && await child(parent, name);
        if (!old) return;
        for (const [entryName, entry] of await entriesOf(old)) {
            if (entry.kind === 'directory' && isAccount(entryName)) {
                const to = await target(entryName);
                for (const [inner, innerEntry] of await entriesOf(entry)) await moveEntry(entry, inner, innerEntry, to);
                await removeIfEmpty(old, entryName);
            } else {
                await moveEntry(old, entryName, entry, await target(account));
            }
        }
        await removeIfEmpty(parent, name);
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
        // Резервная копия: <аккаунт>\FaceTexture_backup\<дата-время>\ — чистые портреты и украшения
        backupCreate: account => areaCreate(BACKUP, account),
        backups: async account => listFolders(await area(BACKUP, account, false)),
        backupDelete: async (account, folder) => removeFrom(await area(BACKUP, account, false), folder),
        backupRead: async (path, name) => readFile(await areaFolder(BACKUP, path), name),
        backupWrite: (path, base64) => areaWrite(BACKUP, path, base64),
        async write(name, base64) {
            const handle = await (await faces()).getFileHandle(name, { create: true });
            const writable = await handle.createWritable();
            await writable.write(fromBase64(base64));
            await writable.close();
            return 'OK:';
        },
        // Текущие портреты: <аккаунт>\Current portraits\<персонаж>.bmp и .txt — чистые основы и украшения
        toolList: async account => listFiles(await area(CURRENT, account, false)),
        toolRead: async (account, name) => readFile(await area(CURRENT, account, false), name),
        async toolWrite(path, base64) {
            const [account, name] = split(path);
            return writeFile(await area(CURRENT, account, true), name, base64);
        },
        toolDelete: async (account, name) => removeFrom(await area(CURRENT, account, false), name),
        // Избранное: <аккаунт>\Favorite FaceTexture\<дата-время>\ — набор всех портретов
        favoriteCreate: account => areaCreate(FAVORITES, account),
        favorites: async account => listFolders(await area(FAVORITES, account, false)),
        favoriteRead: async (path, name) => readFile(await areaFolder(FAVORITES, path), name),
        favoriteWrite: (path, base64) => areaWrite(FAVORITES, path, base64),
        favoriteDelete: async (account, folder) => removeFrom(await area(FAVORITES, account, false), folder),
        // Данные прошлых версий — в папку аккаунта; ids — персонажи аккаунта через перевод строки
        async adoptLoose(account, ids) {
            const root = await savedRoot();
            const tools = await child(root, TOOLS);
            const legacy = tools && await child(tools, LEGACY_TOOL);
            for (const name of [BACKUP, FAVORITES]) {
                const target = owner => area(name, owner, true);
                // самые старые версии держали копии и избранное в корне Black Desert, следующие — в BDO Tools\portraits
                await spreadByAccount(root, name, account, target);
                await spreadByAccount(legacy, name, account, target);
            }
            // чистые портреты и украшения — только персонажей этого аккаунта: чужие дождутся своего аккаунта
            if (legacy) {
                const characters = new Set(ids.split('\n'));
                for (const [name, entry] of await entriesOf(legacy)) {
                    const id = name.substring(0, name.lastIndexOf('.'));
                    if (entry.kind === 'file' && characters.has(id)) await moveEntry(legacy, name, entry, await area(CURRENT, account, true));
                }
                await removeIfEmpty(tools, LEGACY_TOOL);
            }
            await spreadByAccount(tools, LOBBY_BACKUP, account, owner => area(LOBBY_BACKUP, owner, true));
            return 'OK:';
        },
        // Заметки персонажей: Black Desert\UserCache\<аккаунт>\gameVariable_lobby.xml
        lobbyRead: async account => readFile(await child(await child(await savedRoot(), USER_CACHE), account), LOBBY),
        async lobbyWrite(account, base64) {
            const dir = await (await (await savedRoot()).getDirectoryHandle(USER_CACHE)).getDirectoryHandle(account);
            return writeFile(dir, LOBBY, base64);
        },
        // Копия перед записью: <аккаунт>\lobby_backup\<дата-время>.xml
        async lobbyBackup(account, base64) {
            const dir = await area(LOBBY_BACKUP, account, true);
            let name = stamp() + '.xml';
            for (let n = 2; await hasFile(dir, name); n++) name = stamp() + '_' + n + '.xml';
            await writeFile(dir, name, base64);
            return 'OK:' + [TOOLS, TOOL, account, LOBBY_BACKUP, name].join('\\');
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
        // Картинка из буфера обмена: браузер может спросить разрешение; нет картинки или запретили — NO_CLIPBOARD_IMAGE
        async pasteImage() {
            if (!navigator.clipboard || !navigator.clipboard.read) return 'ERR:NO_CLIPBOARD_IMAGE';
            let items;
            try {
                items = await navigator.clipboard.read();
            } catch (e) {
                return 'ERR:NO_CLIPBOARD_IMAGE';
            }
            for (const item of items) {
                const type = item.types.find(t => t.startsWith('image/'));
                if (type) return 'OK:' + await toBase64(await item.getType(type));
            }
            return 'ERR:NO_CLIPBOARD_IMAGE';
        },
    };
}"""
