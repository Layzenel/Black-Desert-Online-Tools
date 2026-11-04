package com.layzen.bdotools.core.folder

/**
 * Общая часть моста к папке `Документы\Black Desert`: ставит в `globalThis.__bdoFolder` функцию `register`,
 * через которую каждая фича подключает свои методы (см. [FolderBridge]).
 *
 * Выбранная папка хранится в одной записи IndexedDB на весь сайт, поэтому после перезагрузки страницы
 * и при переходе на другую тулзу её не нужно выбирать заново — достаточно подтвердить разрешение.
 *
 * Методы отвечают строками `OK:<данные>` или `ERR:<код>`. Кроме методов фичи, у каждого моста есть:
 * - `status` → `OK:none` или `OK:granted:<имя папки>` / `OK:prompt:<имя папки>`;
 * - `request` → снова спрашивает разрешение на сохранённую папку;
 * - `pick` → выбор папки; подходит папка с маркером или папка, внутри которой лежит `Black Desert` с ним.
 *
 * Фабрика фичи получает помощники: `savedRoot`, `child`, `toBase64`, `fromBase64`, `stamp`, `pickFile`.
 */
internal const val FOLDER_BRIDGE_SOURCE = """(() => {
    if (globalThis.__bdoFolder) return;
    const DB = 'bdo-tools', STORE = 'handles', KEY = 'black-desert';

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
    const gameDir = async (dir, marker) => {
        if (await child(dir, marker)) return dir;
        const nested = await child(dir, 'Black Desert');
        return nested && await child(nested, marker) ? nested : null;
    };
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
    // Файл с устройства через обычный <input type=file>; null, если окно закрыли
    const pickFile = accept => new Promise(resolve => {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = accept;
        input.onchange = () => resolve(input.files && input.files[0] ? input.files[0] : null);
        input.oncancel = () => resolve(null);
        input.click();
    });
    const fail = e => 'ERR:' + (e && e.name === 'NotAllowedError' ? 'PERMISSION' : 'UNKNOWN:' + (e && e.message));

    const folderApi = marker => ({
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
            const root = await gameDir(picked, marker);
            if (!root) return 'ERR:NOT_GAME_FOLDER';
            await idb('readwrite', s => s.put(root, KEY));
            return 'OK:' + root.name;
        },
    });
    const helpers = { savedRoot, child, toBase64, fromBase64, stamp, pickFile };

    // Мост фичи в globalThis[name]: методы папки и методы фичи; любое исключение превращается в ERR:<код>
    const register = (name, marker, factory) => {
        if (globalThis[name]) return;
        const api = { ...folderApi(marker), ...factory(helpers) };
        const bridge = {};
        for (const [method, action] of Object.entries(api)) {
            bridge[method] = (a, b) => Promise.resolve().then(() => action(a, b)).catch(fail);
        }
        globalThis[name] = bridge;
    };
    globalThis.__bdoFolder = { register };
})();"""
