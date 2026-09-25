<div align="center">

# DRS Smart Keyboard V 1.10.0

## الجولة الشاملة العاشرة — محرر الحافظة المنبثق الذكي: شاشة كاملة، بحث واستبدال، خوارزميات ذكية، خطوط، ومشاركة

**Tenth Comprehensive Round — The Popup Smart Clipboard Editor: a Full Screen, Find & Replace, Smart Algorithms, Fonts, and Sharing**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.10.0 — محرر النص الذكي المنبثق

- 🖥️ **شاشة منبثقة كاملة بدل الحوار الصغير**: اختيار **«تعديل النص»**
  من قائمة العنصر يفتح الآن **محررًا يكتسح لوحة الحافظة بالكامل** —
  ترويسة بعنوان «محرر النص الذكي» مع زرّي **مشاركة** و**حفظ كملف**
  في الترويسة نفسها، حقل تحرير يمتد على كامل المساحة المتبقية ويتمرر
  رأسيًا، وصفّ إجراءات سفلي — كل شيء على الشاشة الواحدة دون تضييق
- 🔍 **بحث واستبدال داخل النص بمحرك حرفي حقيقي**:
  - حقل **بحث** يعدّ المطابقات لحظيًا على نص حتى 50,000 حرف مع
    عدّاد **«{active} من {count}»** وأزرار **التالي/السابق** بلفّ
    دائري كامل حول النص
  - مفتاح **«مطابقة الحالة»** لتبديل الحساسية لحالة الأحرف فورًا
  - حقل **استبدال** يظهر مع وجود بحث: **استبدال** المطابقة الحالية
    أو **استبدال الكل** بتوست يذكر العدد الحقيقي «تم استبدال N
    موضعًا»
  - المطابقة **حرفية أبدًا وليست تعبيرًا نمطيًا**: البحث عن `a.c`
    أو `$0` يعني حرفيًا تلك المحارف — والنتائج مقيّدة بـ1000
    مطابقة كي يبقى التنقل ذا معنى على النصوص الضخمة
- 🧠 **ستة عشر خوارزمية ذكية كرقائق تطبيق فوري — كلها قابلة للتراجع**:
  - **عائلة الحالات** بشارات لاتينية محايدة: `AA` أحرف كبيرة، `aa`
    صغيرة، `Aa` حالة العنوان (أول حرف من كل كلمة)، `aA` قلب الحالات
  - **جراحة المسافات**: تشذيب الأسطر، ضغط المسافات الأفقية
    (بما فيها غير الفاصلة) مع إبقاء فواصل الأسطر، حذف الأسطر الفارغة
  - **عمليات الأسطر**: إزالة التكرار مع إبقاء أول ظهور وترتيب الأصل،
    ترتيب تصاعدي/تنازلي، عكس ترتيب الأسطر
  - **تطبيع عربي واعٍ**: **إزالة التشكيل** (كل الحركات والشدّة
    والتكرير) و**توحيد الحروف** (أ إ آ ٱ ← ا، ى ← ي، ة ← ه) —
    مثالي قبل البحث أو مشاركة النصوص العربية
  - **مستخرجات ذكية**: **الروابط** (http/https/www)، **الإيميلات**،
    **الأرقام** (بحارس ≥7 أرقام يستبعد السنوات والأرقام القصيرة) —
    كل مستخرج يستبدل النص بالنتائج سطرًا لكل نتيجة مع توست العدد
    الصادق «تم استخراج N» و«لا يوجد ما يُستخرج» عند الفراغ
- ↩️ **تراجع/إعادة حقيقي بخمسين حالة**: كل خوارزمية وكل استبدال يدفع
  الحالة السابقة إلى مكدس مقيّد بخمسين إدخالة (الأقدم يُهمَل)، وزرّا
  **التراجع/الإعادة** يعملان باللفّ الصحيح: أي دفعة جديدة تُلغي فرع
  الإعادة — فجرّب الخوارزميات بلا خوف
- 🔤 **تخصيص خطوط المحرر**: **خمس عائلات خطوط** (افتراضي، سنسريف،
  سريف، أحادي المسافة، مخطوط) × **أربع درجات حجم** (12/14/17/20) —
  الاختيار لحظي على حقل التحرير نفسه، والرقاقة النشطة تُضاء بحالة
  `active` تتبع ثيمتها
- 📤 **مشاركة من موضعين**: زر **مشاركة** في ترويسة المحرر يشارك النص
  قيد التحرير، وفعل **«مشاركة النص»** في قائمة العنصر المطوّلة يشارك
  نص العنصر مباشرة — كلاهما عبر **ورقة مشاركة النظام** (ACTION_SEND
  مع منتقي التطبيقات) وقد صُحّح للسياق غير النشط بعلم NEW_TASK
- 📊 **الإحصاءات الحية والسقف كما هما**: سطر الأحرف/الكلمات/الأسطر
  يتنفس مع كل ضغطة، وعدّاد **{used} / 50,000** يذكّر بسقف التخزين
  المطبق لحظيًا على كل إدخال، والحفظ عبر المسار المحركي المعتمد
  (رفع الطابع فيطفو العنصر، بقاء التثبيت والهوية، ومزامنة
  المقصوصة الأساسية إن كانت هي العنصر)
- 🌐 **29 مفتاح سلاسل AR/EN جديدًا**، بفحص توازٍ آلي (PARITY OK —
  **2169** مفتاحًا لكل لغة).
- ✅ **256 اختبار وحدة ناجحًا** (كانت 233): 23 اختبارًا جديدًا تغطي
  عائلة الحالات على اللاتينية والعربية وحالات الفراغ، جراحة
  المسافات بفواصل الأسطر المحفوظة، عمليات الأسطر بترتيب الأصل،
  إزالة التشكيل وتوحيد الحروف حرفًا بحرف، المستخرجات الثلاثة
  بعقودها (الترتيب وإزالة التكرار وحارس الأرقام)، الطابع الحرفي
  للمحرك (الحروف الخاصة بلا تفسير نمطي)، حدّ 1000 مطابقة، اللفّ
  الدائري وتقييد الفهارس اليتيمة في التنقل، عدد الاستبدال الصادق،
  وعقد التراجع/الإعادة كاملًا (اللفّ، إلغاء فرع الإعادة، السقف
  القابل للتضييق) — إضافة إلى تثبيت الخيارات النقية للخطوط.

## لماذا هذا مهم

الحافظة كانت تخزن وتلصق؛ الآن **تحرّر وتنظّف وتستخرج وتشارك** من دون
مغادرة الكيبورد: صحّح نصًا منسوخًا، نظّف مسافاته، وحّد حروفه العربية
قبل البحث فيه، استخرج روابطه في رسالة، كبّر خطه لتقرأه براحة على
شاشة صغيرة، ثم أرسله إلى أي تطبيق — وكل ذلك **دون إنترنت، ودون
مغادرة جهازك**، وكل خوارزمية نقية مختبرة تعمل محليًا على الجهاز.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.10.0.apk` — ثبّته مباشرة (ترقية
  موضعية آمنة فوق أي إصدار سابق، نفس مفتاح التوقيع)
- **AAB**: للاحتياجات المتقدمة
- **SHA256SUMS.txt**: تحقق تشفيري كامل من كل الأصول
- **حزم السمات** (8): سمات إضافية اختيارية تُثبَّت لاحقًا

## الخصوصية

كل شيء يعمل **دون اتصال بالإنترنت إطلاقًا** على مسار الكتابة. البحث
والخوارزميات والمشاركة تحدث محليًا على جهازك، والمشاركة تمر عبر ورقة
النظام إلى التطبيق الذي تختاره أنت، والحفظ والتصدير يكتبان فقط حيث
تختار، ولا يُطلب إذن تخزين قط — والإحصاءات تبقى عدّادات مجهولة على
جهازك (لا نصوص، لا طوابع زمنية لكل ضغطة، لا هوية حقل) — كما في كل
الجولات السابقة.

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

## What's new in V 1.10.0 — the popup smart text editor

- 🖥️ **A full popup screen instead of the small dialog**: choosing
  **«Edit text»** from an item's long-press menu now opens an editor
  that **takes over the whole clipboard panel** — a «Smart text
  editor» header with **Share** and **Save-as-file** right in it, an
  edit field that fills the remaining height and scrolls vertically,
  and a bottom action row. Everything lives on one screen.
- 🔍 **Real literal find & replace inside the text**:
  - a **find** field counts matches live on texts up to 50,000
    characters, with an **«{active} of {count}»** counter and
    **next/previous** buttons that wrap fully around the text;
  - a **«Case sensitive»** toggle flips match sensitivity instantly;
  - a **replace** field appears with an active query: replace the
    current match or **replace all**, with an honest toast reporting
    the real count;
  - matching is **always literal, never regex**: searching `a.c` or
    `$0` means exactly those characters — and results are capped at
    1000 matches so navigation stays meaningful on huge texts.
- 🧠 **Sixteen smart algorithms as one-tap chips — all undoable**:
  - **the case family** with language-neutral badges: `AA` upper,
    `aa` lower, `Aa` title case (first letter of every word), `aA`
    invert case;
  - **whitespace surgery**: trim lines, collapse horizontal
    whitespace (including non-breaking spaces) while keeping line
    breaks, remove empty lines;
  - **line operations**: dedupe lines keeping the first occurrence
    and original order, sort ascending/descending, reverse line
    order;
  - **Arabic-aware normalization**: **remove diacritics** (all
    harakat, shadda, superscript marks) and **normalize letters**
    (أ إ آ ٱ → ا، ى → ي، ة → ه) — perfect before searching or
    sharing Arabic text;
  - **smart extractors**: **links** (http/https/www), **emails**,
    **phone numbers** (guarded by a ≥7-digit rule that rejects years
    and short runs) — each replaces the text with the found lines,
    one per line, with an honest «Extracted N» toast and a clear
    «Nothing to extract» when empty.
- ↩️ **Real undo/redo with fifty states**: every algorithm and every
  replace pushes the previous state onto a stack capped at fifty
  entries (oldest dropped), and the **undo/redo** buttons walk it
  with correct semantics — any new push voids the redo branch. Try
  the algorithms without fear.
- 🔤 **Editor font customization**: **five font families** (default,
  sans, serif, mono, cursive) × **four size steps** (12/14/17/20) —
  applied instantly to the edit field itself, with the active chip
  lit by a theme-aware `active` state.
- 📤 **Sharing from two places**: a **Share** button in the editor
  header shares the text being edited, and a **«Share text»** action
  in the item's long-press menu shares the item directly — both via
  the **system share sheet** (ACTION_SEND with the app chooser),
  correctly flagged NEW_TASK for the non-activity IME context.
- 📊 **Live stats and the cap, unchanged**: the chars/words/lines row
  breathes with every keystroke, the **{used} / 50,000** counter
  reminds the storage cap applied on every input, and saving goes
  through the established engine path (bumped timestamp floats the
  item, pin and id preserved, primary clip re-synced when it is the
  same item).
- 🌐 **29 new AR/EN string keys**, parity-checked automatically
  (PARITY OK — **2169** keys per language).
- ✅ **256 unit tests passing** (was 233): 23 new tests covering the
  case family on Latin and Arabic and empty inputs, whitespace
  surgery with preserved line breaks, line operations with original
  order kept, diacritic stripping and letter normalization character
  by character, all three extractors with their contracts (order,
  dedupe, the digit guard), the engine's literal matching (special
  characters never interpreted), the 1000-match cap, wrap-around and
  stale-index-clamped navigation, the honest replace count, and the
  full undo/redo contract (walk, redo-voiding, a shrinkable cap) —
  plus the pure font options pinned.

## Why it matters

The clipboard used to store and paste; now it **edits, cleans,
extracts and shares** without leaving the keyboard: fix a copied
text, tidy its whitespace, normalize its Arabic letters before
searching, pull its links into a message, enlarge its font to read
comfortably on a small screen, then send it to any app — all **fully
offline, fully on-device**, with every algorithm a tested pure
function running locally.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.10.0.apk` — install directly (safe
  in-place upgrade over any previous release, same signing key)
- **AAB**: for advanced needs
- **SHA256SUMS.txt**: full cryptographic verification of every asset
- **Theme packs** (8): optional add-on themes, installable later

## Privacy

Everything runs **fully offline**: no internet on the typing path at
all. Search, algorithms and sharing happen locally on your device;
sharing goes through the system sheet to the app **you** choose;
saving and exporting write only where you choose; no storage
permission is ever requested; and statistics remain anonymous
counters on your device (never text, never per-keystroke timestamps,
never field identity) — same as every round before this one.

</div>
