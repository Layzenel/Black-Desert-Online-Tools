package com.layzen.bdotools.feature.screenshoteditor.data

/**
 * Методы моста редактора скриншотов (см. `FolderBridge`): выбор картинки — `имя\nbase64`,
 * сохранение — скачивание файла, копирование — PNG в буфер обмена (браузер принимает только PNG).
 */
internal const val SCREENSHOT_BRIDGE_SOURCE = """helpers => ({
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
})"""
