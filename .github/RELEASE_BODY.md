<div align="center">

# DRS Smart Keyboard V 1.8.0

## الجولة الشاملة الثامنة — شريط المهام فوق الاقتراحات للجميع وزر السحب الجانبي ودرج التثبيت ومؤشرات التفعيل الحقيقية

**Eighth Comprehensive Round — Tasks Bar Above Suggestions for Everyone, Side-Pull Handle, Pinned-Tools Drawer, Real Toggle Indicators**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.8.0 — كل ميزة مربوطة بالمحرك فعليًا

- 🎯 **شريط المهام فوق شريط الاقتراحات — للجميع**: كان الشريط الموحد
  مخفيًا افتراضيًا للنظام العادي (اشتراك اختياري) ومشروطًا بمفاتيح
  الملف الشخصي للنظامين الآخرين؛ صار الآن **الشريط الأول أعلى شريط
  الاقتراحات لكل الأنظمة الثلاثة** (العادي والتقني وكلاهما) بمفتاح
  رئيسي واحد محفوظ `unifiedStripEnabled` افتراضه «ظاهر»، مع بقاء حارس
  حقول كلمة المرور كما هو — المفتاح الواحد الصادق هو الباب الوحيد
  للإخفاء، ومتوفر داخل لوحة المفاتيح نفسها ومن شاشة الأدوات
- 🎚️ **زر السحب الجانبي + درج المهام المثبتة**: أول عنصر في الشريط صار
  مقبض سحب (زر جانبي) يفتح **درج «المهام المثبتة»** فوق مساحة لوحة
  المفاتيح (نفس نمط تبديل اللوحات القائم) — ويظل متاحًا للجميع حتى لو
  أفرغت الشريط من كل البلاطات. الدرج يمنح **كل مستخدم من الأنظمة
  الثلاثة** تحكمًا مباشرًا داخل الكيبورد دون فتح الإعدادات:
  - **«المثبتة الآن»**: بلاطاتك المثبتة بترتيب رأس الشريط مع أزرار
    إعادة ترتيب (أعلى/أسفل) وفك تثبيت حقيقية عبر مسارات `moveTool` و
    `setToolPinned` المحفوظة نفسها
  - **«جميع المهام»**: الكتالوج الكامل (44 أداة) مع مرشحات المجموعات
    (الكل/أدوات/تحرير/المؤشر)، وتثبيت/فك تثبيت، وإظهار/إخفاء لكل أداة
  - **عقد «التثبيت للجميع» الجديد**: تثبيت أداة تقنية من الوضع البسيط
    كان سابقًا يبتلع التثبيت صامتًا (مثبتة لا تُرى أبدًا) — صار
    التثبيت يوسّع نطاق ظهور الأداة تلقائيًا عبر `ensureVisibleOverride`
    النقية فتظهر فعلًا في مستواك الحالي، والارتداد مختبر
  - **مفتاح الشريط الرئيسي وإعادة الضبط** في ذيل الدرج
- 💡 **مؤشرات التفعيل الحقيقية على بلاطات الشريط**: أدوات التبديل
  (الوضع الخفي/التصحيح التلقائي/صف الأرقام/إظهار الشريط الذكي/النافذة
  العائمة) تعرض الآن نقطة تفعيل بلون نظامك عند اشتغالها — تُقرأ من
  مصادرها المحركية الحقيقية نفسها (رايات حالة الإدخال، وإعدادات jetpref،
  ومتحكم النافذة) لا من تقدير، عبر بنية `ToggleStates` النقية المختبرة
- 🛠️ **أداتا كتالوج جديدتان** (تصبح **44 أداة**) بمسارات محرك قائمة
  فعلًا، ملحقتان بذيل الكتالوج حفاظًا على ترتيبك وتثبيتك المحفوظَين:
  **«الإجراءات السريعة»** عبر `TOGGLE_ACTIONS_OVERFLOW` (لوحة الأكثر
  استخدامًا والإجراءات المخفية — أسرع وصول لكل المهام)، و**«محرر
  الإجراءات»** عبر `TOGGLE_ACTIONS_EDITOR` — كلتاهما مسجلتان في
  الأدوات الذكية فتُحتسبان في بلاطات الأكثر استخدامًا من اليوم الأول
- ✍️ **عمليتان نصيتان جديدتان** (تصبح **47 عملية**)، كلتاهما تحويل نقي:
  - **فصل الأرقام عن الحروف**: مسافة واحدة بين كل رقم وحرف متجاور في
    الاتجاهين (12abc تصبح 12 abc، و٣س تصبح ٣ س) — `p{N}` يشمل
    العربية-الهندية ٠-٩ و`p{L}` يشمل العربية واللاتينية، والـ lookaround
    لا يستهلك محارف فلا تُمس الترقيمات والأسطر، والعملية idempotent
  - **إزالة علامات الترقيم**: يحذف كل علامات يونيكود الفئتية P (،؛؟
    العربية واللاتينية وعلامات الاقتباس والأقواس) مع إبقاء الحروف
    والأرقام والمسافات والرموز والعملات — شقيق «تنظيف النص» الموجّه
    للبحث والعدّ
- 🌐 **19 مفتاح سلاسل AR/EN جديدًا**، مفحوص التوازي آليًا
  (PARITY OK — 2124 مفتاحًا لكل لغة)
- ✅ **214 اختبار وحدة ناجحًا** (كانت 198): مصفوفة `ensureVisibleOverride`
  كاملة (توسيع تقني↔بسيط، وترك المرئي كما هو، وطفو التثبيت لرأس
  الشريط)، وعقود الأداتين الجديدتين من الرمز إلى السجل، واستقلالية راية
  درج الأدوات عن راية الفائض، وترحيل حالة v1.7.0 القديمة (تُفتح
  بشارط ظاهر)، والعمليتين النصيتين بأطرافهما (العربية واللاتينية
  والعملات والidempotency)، وتحديث نوافذ عقود الذيل للجولات 1.4.0–1.7.0
  (+2)

## بعد التثبيت

افتح أي حقل كتابة — ستجد **شريط المهام أعلى شريط الاقتراحات مباشرة**.
اضغط زر السحب الجانبي (أول عنصر) لفتح درج المهام المثبتة: ثبّت ما
تستخدمه، أعد ترتيبه، أخف ما لا تريده — وبنقطة التفعيل على بلاطات
التبديل ستعرف حالة كل مفتاح بنظرة. شاشة التشخيصات «الفحص التقني
الشامل» يواصل التحقق من 32 بندًا حقيقيًا، وكل شيء من بياناتك المحلية
وحدها.

## الخصوصية

كل شيء يعمل **دون اتصال بالإنترنت إطلاقًا** في مسار الكتابة،
والإحصاءات عدّادات مجهولة على جهازك (لا نصوص ولا طوابع زمنية للضغطات
ولا هوية الحقول)، والنسخ الاحتياطي ملف JSON محلي تختار وجهته بنفسك
عبر منتقي ملفات النظام — والحالة الجديدة ترحّل آليًا: من يرقّى من
V 1.7.0 يجد الشريط ظاهرًا للجميع دون أي فقدان لتخصيصاته المحفوظة.

</div>

---

<div align="center">

## About

**DRS Smart Keyboard** is a free, open-source (Apache-2.0) Android keyboard
built with Kotlin, Jetpack Compose and Material 3 — **Arabic-first**: a
fully localized Arabic UI with native RTL, carefully designed Arabic
layouts, and Arabic suggestions and correction, with English as a complete
second option. Everything you type stays on your device: no accounts, no
tracking, no ads.

## What's new in V 1.8.0 — every feature wired to the real engine

- 🎯 **The tasks bar now sits ABOVE the suggestions strip — for
  everyone**: the unified strip used to be hidden by default for the
  TYPICAL system (opt-in) and gated by profile switches elsewhere; it
  is now the FIRST row above suggestions on ALL three systems, with a
  single persisted master switch (`unifiedStripEnabled`, default ON)
  and the password-field guard. One honest way off, available both
  inside the keyboard drawer and the tools screen.
- 🎚️ **Side-pull handle + pinned-tools drawer**: the strip's first
  element is now a drag-handle button that opens the **«Pinned tools»
  drawer** over the keyboard area (the established panel-swap pattern)
  — reachable for everyone even with an empty strip. It gives every
  user of all three systems direct in-keyboard control:
  - **Pinned now**: your pinned tiles in strip-head order with real
    reorder (up/down) and unpin through the same persisted
    `moveTool`/`setToolPinned` paths
  - **All tools**: the full 44-tool catalogue with group filters
    (all/tools/editing/cursor), pin/unpin and show/hide per tool
  - **The new pin-for-everyone contract**: pinning a technical-only
    tool from the simple level used to swallow the pin silently
    (pinned, never rendered) — pinning now widens the tool's view
    override via the pure `ensureVisibleOverride` helper so it really
    appears on your current level, with round-trip tests
  - **Bar master switch and reset** at the drawer's tail
- 💡 **Real on/off indicators on strip tiles**: toggle tools
  (incognito / autocorrect / number row / smartbar visibility /
  floating window) now show an accent dot while active — read from the
  very engine sources that own the state (IME state flags, jetpref
  settings, the window controller) through the tested pure
  `ToggleStates` structure.
- 🛠️ **Two new catalogue tools (44 total)** on real engine actions,
  tail-appended: **Quick actions** via `TOGGLE_ACTIONS_OVERFLOW` (the
  most-used + hidden-actions panel — the fastest "all tools" surface)
  and **Actions editor** via `TOGGLE_ACTIONS_EDITOR` — both already in
  SmartToolCodes, so they count in most-used stats from day one.
- ✍️ **Two new pure text operations (47 total)**: **Separate digits
  from letters** (one space at every digit↔letter boundary, both
  directions — `\p{N}` covers ٠-۹ and 0-9, `\p{L}` covers Arabic and
  Latin; lookarounds consume nothing so punctuation and newlines
  survive; idempotent) and **Remove punctuation** (every Unicode
  category-P mark, Arabic ،؛؟ and Latin alike, keeping letters, digits,
  spaces, symbols and currency).
- 🌐 **19 new AR/EN string keys**, parity-checked automatically
  (PARITY OK — 2124 keys per language).
- ✅ **214 unit tests passing** (was 198): the full
  `ensureVisibleOverride` matrix, the new tools' code-to-registry
  contracts, the drawer flag's independence from the overflow flag,
  old-v1.7.0-state migration (decodes with the bar ON), both text
  operations' edge cases (Arabic/Latin/currency/idempotency), and the
  v1.4.0–v1.7.0 tail contracts shifted by the two new tools.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.8.0.apk` — install directly (safe
  in-place upgrade over any previous release, same signing key)
- **AAB**: for advanced needs
- **SHA256SUMS.txt**: full cryptographic verification of every asset
- **Theme packs** (8): optional add-on themes, installable later

## Privacy

Everything runs **fully offline**: no internet on the typing path at all.
Statistics are anonymous counters on your device (never text, never
per-keystroke timestamps, never field identity), and backups are local
JSON files whose destination you pick through the system file picker.
The new state migrates automatically: upgrading from V 1.7.0 turns the
bar on for everyone without touching any saved customization.

</div>
