package com.layzen.bdotools.feature.comboassist.data

/** Исходник JS-моста файлов комбо: выбор `.json` и скачивание текста. Помощник `pickFile` — из `FolderBridge`. */
internal const val COMBO_BRIDGE_SOURCE = """({ pickFile }) => {
    const readText = async file => new TextDecoder('utf-8', { ignoreBOM: true }).decode(await file.arrayBuffer());
    return {
        async pickJson() {
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
