package com.layzen.bdotools.feature.screenshoteditor.data

/**
 * Методы моста редактора скриншотов (см. `FolderBridge`): выбор картинки — `имя\nbase64`,
 * сохранение — скачивание файла, копирование — PNG в буфер обмена (браузер принимает только PNG);
 * галерея папки `ScreenShot` — список `имя\tвремя` по строке, файл и миниатюра JPEG в base64.
 */
internal const val SCREENSHOT_BRIDGE_SOURCE = """helpers => {
    const shots = async () => {
        const root = await helpers.savedRoot();
        const dir = root && await helpers.child(root, 'ScreenShot');
        if (!dir) throw new Error('no ScreenShot folder');
        return dir;
    };
    const shot = async name => (await (await shots()).getFileHandle(name)).getFile();
    return {
        async pickImage() {
            const file = await helpers.pickFile('image/*');
            return file ? 'OK:' + file.name + '\n' + await helpers.toBase64(file) : 'ERR:CANCELLED';
        },
        async save(name, data) {
            const link = document.createElement('a');
            link.href = URL.createObjectURL(new Blob([helpers.fromBase64(data)]));
            link.download = name;
            link.click();
            setTimeout(() => URL.revokeObjectURL(link.href), 60000);
            return 'OK:';
        },
        async copyImage(data) {
            if (!navigator.clipboard || !navigator.clipboard.write || typeof ClipboardItem === 'undefined') return 'ERR:CLIPBOARD';
            try {
                const blob = new Blob([helpers.fromBase64(data)], { type: 'image/png' });
                await navigator.clipboard.write([new ClipboardItem({ 'image/png': blob })]);
                return 'OK:';
            } catch (e) {
                return 'ERR:CLIPBOARD';
            }
        },
        async list() {
            const lines = [];
            for await (const [name, handle] of (await shots()).entries()) {
                if (handle.kind === 'file') lines.push(name + '\t' + (await handle.getFile()).lastModified);
            }
            return 'OK:' + lines.join('\n');
        },
        async read(name) {
            return 'OK:' + await helpers.toBase64(await shot(name));
        },
        // Миниатюру уменьшает сам браузер: в Kotlin пришлось бы гонять весь файл через base64
        async thumb(name) {
            const bitmap = await createImageBitmap(await shot(name), { resizeWidth: 360, resizeQuality: 'medium' });
            const canvas = new OffscreenCanvas(bitmap.width, bitmap.height);
            canvas.getContext('2d').drawImage(bitmap, 0, 0);
            bitmap.close();
            return 'OK:' + await helpers.toBase64(await canvas.convertToBlob({ type: 'image/jpeg', quality: 0.8 }));
        },
    };
}"""
