package com.layzen.bdotools.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Палитра дизайн-системы. Значения сняты пипеткой со скриншотов окон игры
 * (меню, настройки, выбор персонажа) в `.claude/references/ui`.
 */
@Immutable
data class AppColors(
    // Фоны, от самого глубокого к самому приподнятому
    val backgroundDeep: Color,
    val backgroundSurface: Color,
    val backgroundRaised: Color,
    val backgroundItem: Color,
    val backgroundInput: Color,
    val backgroundSelected: Color,

    // Шапка окна
    val titleBar: Color,
    val titleBarDark: Color,
    val titleBarLight: Color,
    val titleBarHighlight: Color,

    // Рамка окна, как у окон игры: тонкая тёмная снаружи, светлая бронзовая внутри
    val windowOutline: Color,
    val windowFrame: Color,

    // Кнопки
    val button: Color,
    val buttonHover: Color,
    val buttonPressed: Color,
    val buttonBorder: Color,

    // Обычная кнопка действия ([ActionButton]), как «Назад» в настройках игры: серая подложка чуть темнеет книзу,
    // светлая кромка внутри тёмной рамки; под курсором — коричневая, недоступная — плоская серая
    val actionButtonTop: Color,
    val actionButtonBottom: Color,
    val actionButtonEdge: Color,
    val actionButtonHoverTop: Color,
    val actionButtonHoverBottom: Color,
    val actionButtonHoverEdge: Color,
    val actionButtonDisabled: Color,
    val actionButtonDisabledEdge: Color,

    // Кнопка положительного действия (применить, сохранить, OK): бронза темнеет к середине и снова светлеет книзу
    val buttonPositiveTop: Color,
    val buttonPositiveMiddle: Color,
    val buttonPositiveBottom: Color,

    // Кнопка опасного действия (удалить): тёмно-красная бронза темнеет книзу
    val buttonNegativeTop: Color,
    val buttonNegativeBottom: Color,

    // Вариант с индикатором ([OptionButton]): подложка и рамка невыбранного и наведённого, квадратик-индикатор и его отметка
    val option: Color,
    val optionBorder: Color,
    val optionHover: Color,
    val optionHoverBorder: Color,

    // Переключатель ([ToggleSwitch]), как в настройках игры: выключен — тёмная дорожка и серый кружок внутри,
    // включён — тёмное золото и крупный кремовый кружок во всю высоту
    val toggleTrack: Color,
    val toggleTrackBorder: Color,
    val toggleThumbOff: Color,
    val toggleOn: Color,
    val toggleThumbOn: Color,
    val indicator: Color,
    val indicatorBorder: Color,
    val indicatorBorderSelected: Color,
    val indicatorMark: Color,
    val indicatorMarkShade: Color,

    // Линии
    val border: Color,
    val borderSelected: Color,
    val divider: Color,
    val track: Color,

    // Текст
    val textPrimary: Color,
    val textBeige: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val textButton: Color,
    /** Подпись выбранного варианта. */
    val textSelected: Color,
    /** Заметка персонажа, как оранжевая подпись в окне «Персонажи аккаунта» игры. */
    val textNote: Color,

    // Акценты
    val accentGold: Color,
    val accentGoldPale: Color,
    val danger: Color,
    val success: Color,
    val accentBlue: Color,
    val accentRed: Color,
    val badgeNew: Color,
    val badgeHot: Color,
    val onBadge: Color,

    // Силуэт индикатора загрузки
    val silhouette: Color,
    val silhouetteOutline: Color,

    // Гнев Чёрного духа: подложка трекера под гневом, от чёрного к языкам пламени
    val rageDeep: Color,
    val rageDark: Color,
    val rageBorder: Color,
    val rageFlame: Color,
    val rageEmber: Color,
)

/** Значения палитры, снятые со скриншотов окон игры. */
val DefaultAppColors = AppColors(
    backgroundDeep = Color(0xFF18181A),
    backgroundSurface = Color(0xFF222226),
    backgroundRaised = Color(0xFF2B2B2F),
    backgroundItem = Color(0xFF34343A),
    backgroundInput = Color(0xFF1E1E21),
    backgroundSelected = Color(0xFF4F433C),

    titleBar = Color(0xFF775D3A),
    titleBarDark = Color(0xFF5E4A2E),
    titleBarLight = Color(0xFF85693F),
    titleBarHighlight = Color(0xFFC0A850),

    windowOutline = Color(0xFF151C24),
    windowFrame = Color(0xFF6A593E),

    button = Color(0xFF44444E),
    buttonHover = Color(0xFF52525D),
    buttonPressed = Color(0xFF38383F),
    buttonBorder = Color(0xFF4E4F56),

    actionButtonTop = Color(0xFF4C4D57),
    actionButtonBottom = Color(0xFF42434D),
    actionButtonEdge = Color(0xFF686A73),
    actionButtonHoverTop = Color(0xFF5E4D44),
    actionButtonHoverBottom = Color(0xFF53443C),
    actionButtonHoverEdge = Color(0xFF806652),
    actionButtonDisabled = Color(0xFF424243),
    actionButtonDisabledEdge = Color(0xFF575758),

    buttonPositiveTop = Color(0xFF9F7B59),
    buttonPositiveMiddle = Color(0xFF704E38),
    buttonPositiveBottom = Color(0xFF8C6144),
    buttonNegativeTop = Color(0xFF603730),
    buttonNegativeBottom = Color(0xFF442620),

    option = Color(0xFF35353A),
    optionBorder = Color(0xFF4D4D55),
    optionHover = Color(0xFF625244),
    optionHoverBorder = Color(0xFF866550),

    toggleTrack = Color(0xFF34343A),
    toggleTrackBorder = Color(0xFF4D4E55),
    toggleThumbOff = Color(0xFF686868),
    toggleOn = Color(0xFF926C2D),
    toggleThumbOn = Color(0xFFFFEDD4),
    indicator = Color(0xFF292929),
    indicatorBorder = Color(0xFF4C4E58),
    indicatorBorderSelected = Color(0xFF7E6454),
    indicatorMark = Color(0xFFFFF2E0),
    indicatorMarkShade = Color(0xFFE4C89F),

    border = Color(0xFF39393F),
    borderSelected = Color(0xFF685048),
    divider = Color(0xFF353536),
    track = Color(0xFF101011),

    textPrimary = Color(0xFFF8F0D8),
    textBeige = Color(0xFFE0C8A0),
    textMuted = Color(0xFFA89068),
    textDisabled = Color(0xFF686868),
    textButton = Color(0xFFE8E8E8),
    textSelected = Color(0xFFF3D7AD),
    textNote = Color(0xFFE0783C),

    accentGold = Color(0xFFF8C038),
    accentGoldPale = Color(0xFFD8B080),
    danger = Color(0xFFD86048),
    success = Color(0xFF7CC050),
    accentBlue = Color(0xFF4F8CD8),
    accentRed = Color(0xFFC83C3C),
    badgeNew = Color(0xFFD09820),
    badgeHot = Color(0xFFD05020),
    onBadge = Color(0xFFF8F8F8),

    silhouette = Color(0xFF000000),
    silhouetteOutline = Color(0xFFE0C8A0),

    rageDeep = Color(0xFF0C0505),
    rageDark = Color(0xFF3A0A0A),
    rageBorder = Color(0xFF7A1414),
    rageFlame = Color(0xFFB4140E),
    rageEmber = Color(0xFFE8501C),
)
