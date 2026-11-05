package com.layzen.bdotools.core.account

/** Разделитель записей в ответе метода `accounts`. */
internal const val ACCOUNT_SEPARATOR = "\n<!--bdo-split-->\n"

/**
 * Методы списка аккаунтов для общего моста к папке `Black Desert` ([com.layzen.bdotools.core.folder.FolderBridge]).
 * - `accounts` → записи «папка\nвремя изменения\nкусок CharacterOrderList» через [ACCOUNT_SEPARATOR]: сам файл
 *   большой, а нужен только список персонажей;
 * - `face(имя)` → портрет из `FaceTexture` в base64 или пустая строка, если файла нет.
 */
internal const val ACCOUNTS_BRIDGE_SOURCE = """({ savedRoot, child, toBase64 }) => {
    const SPLIT = '\n<!--bdo-split-->\n';
    const START = '<CharacterOrderList Version', END = '</CharacterOrderList>';

    return {
        async accounts() {
            const cache = await child(await savedRoot(), 'UserCache');
            if (!cache) return 'OK:';
            const records = [];
            for await (const [id, folder] of cache.entries()) {
                if (folder.kind !== 'directory') continue;
                for await (const [name, handle] of folder.entries()) {
                    if (handle.kind !== 'file' || name.toLowerCase() !== 'gamevariable.xml') continue;
                    const file = await handle.getFile();
                    const text = await file.text();
                    const start = text.indexOf(START);
                    if (start < 0) continue;
                    const end = text.indexOf(END, start);
                    records.push(id + '\n' + file.lastModified + '\n' + text.substring(start, end < 0 ? text.length : end));
                }
            }
            return 'OK:' + records.join(SPLIT);
        },
        async face(name) {
            try {
                const faces = await child(await savedRoot(), 'FaceTexture');
                return 'OK:' + await toBase64(await (await faces.getFileHandle(name)).getFile());
            } catch (e) {
                return 'OK:';
            }
        },
    };
}"""
