<div align="center">

# DRS Smart Keyboard V 1.7.0

## الجولة الشاملة السابعة — أربع قنوات ميتة تُحييَتان وأداة تثبيت وسبع عمليات نصية وإصلاح كثافة الأثر ومعاينة النسخ الاحتياطية

**Seventh Comprehensive Round — Four Dead Channels Revived, Pin Tool, Seven Text Ops, Trail Density Fix, Backup Preview**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.7.0 — كل ميزة مربوطة بالمحرك فعليًا

- 🐛 **أربع قنوات ميتة حقيقية صارت حية**:
  - **بلاطتا «النافذة العائمة» و«وضع التغيير» لم تُعرَضَا قط**: استخدامها
    مُحتسب في إحصاء «الأكثر استخدامًا» منذ V 1.4.0/V 1.5.0، لكن رمزيهما
    لم يكونا في قائمة المفاتيح الداخلية فكان باني البلاطات يُسقطهما صامتًا
    (`getCodeInfoAsTextKeyData` يعيد null) — عقد الخطأ نفسه الذي وثّقه
    إصلاح V 1.3.0 لصف الأرقام، ومُصلَح بالطريقة نفسها
  - **أداة «اللغة» لم تُحتسب قط**: تعمل منذ إصلاح V 1.5.0 لكن رمزها
    `SHOW_SUBTYPE_PICKER` لم يكن في سجل الأدوات الذكية فلم يظهر يومًا في
    بلاطات الأكثر استخدامًا — صار مُسجلًا ومُعنوَنًا ومُعلَّقًا بالتلميح
  - **مفتاح `IME_SUBTYPE_PICKER` كان صامتًا منذ V 1.0.x**: له بيانات
    مفتاح معرفة مسبقًا وبيانات داخلية لكن بلا معالج — ضغطه كان يُسجل
    «مفتاحًا مجهولًا»؛ صار جسرًا إلى منتقي اللغات الداخلي نفسه
  - **مفتاح «تعديل الإجراءات» بلا هوية عرض**: معالجه قائم منذ البداية
    لكنه كان خارج سجلي الاسم والتلميح — أي بلاطة مستقبلية لكانت ستعرض
    العنصر الفاشل العام؛ صار مُسجلًا بالاسم والتلميح الكاملين
- 🛠️ **أداة كتالوج جديدة** (تصبح **42 أداة**) بفعل محرك قائم فعلًا،
  ملحقة بذيل الكتالوج حفاظًا على ترتيبك وتثبيتك المحفوظَين:
  **«تثبيت المقصوصة النشطة»** عبر `CLIPBOARD_PIN_ACTIVE` — تثبيت/فك
  تثبيت المقصوصة الحالية بمسار `pinClip/unpinClip` نفسه الذي تستخدمه
  لوحة الحافظة، مع toast بالحالة الجديدة؛ والمقصوصة المثبتة تنجو من مسح
  السجل بالعقد الموثق — زر «احتفظ بهذا» حقيقي بضغطة واحدة
- ✍️ **سبع عمليات نصية جديدة** (تصبح **45 عملية**)، كلها تحويل نقي:
  - **إصلاح أشكال العرض العربية**: النص المنسوخ من ملفات PDF وبعض
    المواقع يصل بأحرف عرض منفصلة (U+FB50–U+FEFF) تكسر البحث وتكون
    غير مرئية تمامًا لعملية «توحيد الحروف» — NFKC يعيدها إلى الحروف
    الأساسية مع رباط «لا»، والنص السليم يمر كما هو (idempotent)
  - **قائمة إلى أسطر / أسطر إلى قائمة**: الزوج النقيض الواعي باللغة —
    يقبل الفواصل اللاتينية والعربية (، ؛) وينظف المسافات، والدمج يستخدم
    الفاصلة العربية (،) للعربية واللاتينية (,) لغيرها، والأسطر الفارغة
    تُتخطى — خطوة طبيعية قبل «ترقيم الأسطر»
  - **إزالة كل المسافات**: أسلوب الوسوم `#وسم_عربي` — يحذف كل مسافة
    أفقية (بما فيها NBSP) ويبقي فواصل الأسطر كما هي، الشقيق الأشد من
    «تنظيف المسافات» الذي يبقي مسافة واحدة بين الكلمات
  - **تجريد الإيموجي**: يحذف الإيموجي والرموز التعبيرية ومحدداتها
    (VS16 والمنضمات والنطاق التكميلي U+1F000–U+1FAFF) من النص الملصوق
    فلا تشوّش عدادات العد والترتيب والبحث
  - **ترميز رابط / فك ترميزه**: الزوج الكامل لروابط UTF-8 المئوية —
    «سلام» تصبح `%D8%B3%D9%84%D8%A7%D9%85` والمسافة `%20` (وليس +) وفق
    RFC 3986، وفك الترميز التالف يعيد المدخل كما هو دون إتلاف النص
    أبدًا؛ والارتداد الكامل بين الزوجين مُختبر
- 🎨 **إصلاح كثافة أثر التمرير + مقياس عرضه (70–200%)**: نصف قطر
  الشريط المرسوم كان **20 بكسل خام** — أي ~6.7dp على شاشة كثافة 3x
  و~13dp على كثافة 1.5x: نفس الإعداد يعطي شريطًا رفيعًا على الهواتف
  الحديثة وسميكًا على القديمة. صار يُشتق من 20dp محولة بالكثافة الحقيقية
  لحظة الرسم، ومضروبًا بإعداد `glide__trail_width_percent` يُقرأ لحظة
  كل إطار، وحركة التلاشي صارت تحرك **كسرًا** (1→0) بدل بكسلًا خامًا
  فبقي التلاشي مطابقًا للشريط الحي على كل الكثافات — مع شريط تمرير في
  المجموعة الأساسية وثوابت تحقق مشتركة
- 🎨 **مقياس حجم الإيموجي (70–200%)**: خلية شبكة الإيموجي (42dp) وحجم
  الرمز (22sp) كانا ثابتين مضمنين — صارا يتبعان إعداد
  `emoji__size_percent` في الشبكة التكيفية وشبكة نتائج البحث ونافذتي
  المتغيرات والسجل معًا، مع شريط تمرير في المجموعة الأساسية
- 🎨 **نص بحث الحافظة يتبع الثيم أخيرًا**: كان لونه **أبيض مضمنًا** —
  غير مرئي على أي ثيم فاتح (drs_day وأخواته) أثناء الكتابة؛ صار يتبع
  لون مقدمة نافذة الثيم، مع احتياطي مشتق من إضاءة الخلفية (دالة نقية
  `readableTextColor` مُختبرة)
- 📊 **خط أنابيب أنماط السياق من طرف إلى طرف**: وضع السياق
  (كلمة مرور/أرقام/برمجة/كتابة…) كان يُكتشف عند بدء كل إدخال ثم **يُرمى**
  — صار يُحتسب الآن كمّاد بدايات إدخال مجهولة تتدفق من
  `DrsRuntimeState` إلى محرك التكيف (`recordContextStart`) فإلى حالة
  الاستخدام والدلاء اليومية (خريطة مقيدة بأسماء الأوضاع المعروفة حتى لا
  تتسرب مفاتيح مزورة حتى من ملف حالة تالف)، وتُعرض في بطاقة الإجماليات
  كسطر «توزيع أنماط الإدخال» بأسماء حقيقية محلية، وتُصدَّر في عمود CSV
  الثاني عشر
- 📈 **تجميعة إحصاء جديدة**: `topContextModes` — أعلى الأوضاع تكرارًا
  تنازليًا بتعادل حتمي (ترتيب بالاسم) فلا يهتز العرض بين الإطارات،
  والدوام الصفري يعطي قائمة فارغة فيختفي السطر
- 💾 **معاينة النسخة الاحتياطية قبل الاستيراد**: الاستيراد كان يطبق
  الحالة **فورًا بلا أي عرض** — الإجراء الهدمي الوحيد غير المؤكد في
  التطبيق. صار الملف يُقرأ ويُفك أولًا ثم يُعرض حوار تأكيد يصف محتواه
  الحقيقي (الاختصارات، الأنظمة، رصيد المحفظة الإجمالي، أيام الإحصاءات)
  قبل التنفيذ؛ والتنفيذ نفسه عبر `importParsed` وهو نصف
  `importFrom` الحقيقي المنفصل — نفس التحقق ونفس مسار الكتابة بالضبط
- 🩺 **فحص تشخيص جديد** (تصبح **32 فحصًا**): **سلامة قوالب
  الاختصارات** — التوسيع كان يُبقي المتغيرات غير المعروفة نصيًا حرفيًا،
  فخطأ كتابة مثل `{Datee}` كان يُدرج حرفيًا في كل توسيع بلا أي إشارة
  قط؛ الفحص يمر على كل اختصار مفعّل ويتحقق أن كل متغير مكتوب الشكل
  من المجموعة المعروفة (غير حساس للحالة)، ولا قوالب أصلًا يعني نجاحًا
  بلا إنذار كاذب
- 📤 **تصدير التقرير التشخيصي أخيرًا**: مولّد `renderReport()` كان
  موجودًا وكاملًا منذ V 1.0.6 **ودونه أي مستدعٍ واحد** — زر «تصدير
  التقرير» في بطاقة سجل الأحداث يكتب الآن التقرير (الإصدار + ملخص
  الفحص + الأحداث) عبر منتقي ملفات النظام بتوست نجاح/فشل
- 🌐 **50 مفتاح سلاسل جديدًا** بتوافق عربي/إنجليزي تام (PARITY OK —
  2105 مفتاحًا لكل لغة).
- ✅ **198 اختبار وحدة ناجح** (كانت 179): العمليات السبع بأطرافها
  (أشكال العرض والرباط والسلبية idempotent، الفواصل العربية واللاتينية،
  الارتداد الكامل لزوج الرابط، بقاء فواصل الأسطر، عدم مساس الأحرف)،
  فخ الرموز ذات الخمس خانات في regex (`\uXXXX` أربع خانات حرفًا —
  اصطاده اختبار الإيموجي في أول تشغيل وأُصلح بصيغة `\x{...}`)، أداة
  التثبيت من الرمز إلى السجل إلى البيانات الداخلية وعقد الذيل الممتد،
  حدود المقياسين الجديدين ولون النص المقروء، خط أنابيب السياق من
  الدمج إلى العقلنة ورفض المفاتيح المزورة، معاينة النسخة الاحتياطية
  بعدّاداتها الحقيقية، وفحص القوالب بأمثلة القبول والرفض والصمت.

## التنزيل

- **APK**: `DRS-Smart-Keyboard-v1.7.0.apk` — ثبّته مباشرة (الترقية موضعية
  آمنة فوق أي إصدار سابق، نفس مفتاح التوقيع)
- **AAB**: للحاجة المتقدمة
- **SHA256SUMS.txt**: تحقق تشفيري كامل من كل الملفات
- **حزم السمات** (8): سمات إضافية اختيارية بتثبيت لاحق

## التحقق من التكامل

بعد التثبيت: شاشة التشخيصات ← «الفحص التقني الشامل» يتحقق الآن من 32
بندًا حقيقيًا داخل العملية نفسها (بينه فحص جديد لسلامة قوالب
الاختصارات)، و«تصدير التقرير» يكتب تقريرًا كاملًا من سجل الأحداث، وشاشة
الإحصاءات تعرض «توزيع أنماط الإدخال» مع عمود السياق الجديد في CSV —
كلها من بياناتك المحلية وحدها.

## الخصوصية

كل شيء يعمل **دون اتصال بالإنترنت إطلاقًا** في مسار الكتابة،
والإحصاءات عدّادات مجهولة على جهازك (لا نصوص ولا طوابع زمنية للضغطات
ولا هوية الحقول — أنماط السياق مجرد سمة **مكتشفة** لحقل الإدخال)،
والنسخ الاحتياطي ملف JSON محلي تختار وجهته بنفسك عبر منتقي ملفات
النظام مع معاينة تأكيد قبل أي استبدال.

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

## What's new in V 1.7.0 — every feature wired to the real engine

- 🐛 **Four genuinely dead channels are alive**: the floating-window and
  resize-mode tiles were counted in most-used stats since V 1.4.0/V 1.5.0
  but never rendered (their codes were missing from InternalKeys — the
  same bug class documented by the v1.3.0 number-row fix); the LANGUAGE
  tool never counted at all (SHOW_SUBTYPE_PICKER was absent from
  SmartToolCodes); IME_SUBTYPE_PICKER had predefined key data but no
  handler since V 1.0.x (silent "unknown key"); and the actions-editor
  toggle had no display identity (any future tile would have rendered
  the invalid-fatal placeholder).
- 🛠️ **One new catalogue tool (42 total)** on a real engine action,
  tail-appended: **Pin active clip** via `CLIPBOARD_PIN_ACTIVE` — the
  exact pinClip/unpinClip path the clipboard panel uses, with a state
  toast; pinned clips survive history wipes by contract. A real one-tap
  "keep this".
- ✍️ **Seven new pure text operations (45 total)**: Arabic
  presentation-forms repair (NFKC folding of PDF/web glyphs that break
  search and are invisible to letter unification, lam-alef ligature
  included), list↔lines (Arabic/Latin separators, locale-aware join —
  the established inverse-pair convention), remove-all-spaces (hashtag
  style, newlines kept), strip-emoji (pictographs plus selectors and
  the supplementary-plane range), and the URL encode/decode pair (UTF-8
  percent encoding, %20 per RFC 3986, malformed input returned
  untouched; full roundtrip tested).
- 🎨 **Glide trail density fix + width scale (70–200%)**: the trail
  radius was RAW PIXELS (20px ≈ 6.7dp on a 3x phone vs 13dp on 1.5x) —
  it is now derived from 20dp through the real density at draw time,
  multiplied by a `glide__trail_width_percent` setting read per frame,
  and the fade-out animates a FRACTION so the fading ribbon matches the
  live one on every density.
- 🎨 **Emoji size scale (70–200%)**: the fixed 42dp grid cell and 22sp
  glyph now follow an `emoji__size_percent` setting across the adaptive
  grid, the search grid and both popups, with a slider in the basic
  group.
- 🎨 **Clipboard search text follows the theme**: it was hardcoded
  WHITE — invisible on light themes; it now uses the themed window
  foreground with a luminance-derived fallback (`readableTextColor`,
  pure and tested).
- 📊 **Context-mode pipeline end to end**: the detected context mode
  (password/numbers/coding/writing/…) was discarded at every input
  start — it is now counted as anonymous input starts flowing from
  DrsRuntimeState through the adaptation engine into the usage stats
  and daily buckets (the map is capped to known mode names, so even a
  tampered state file cannot smuggle keys), displayed as a localized
  "input mode mix" totals row and exported as a 12th CSV column.
- 📈 **New pure aggregation**: `topContextModes` — descending with a
  deterministic name-order tie-break so the display never flickers.
- 💾 **Describe-before-restore backup preview**: import used to apply
  immediately with zero preview — the app's only destructive
  unconfirmed action. The file is parsed first and a confirm dialog
  shows its real contents (shortcuts, profiles, total wallet balance,
  stats days) before `importParsed` runs the exact same validation and
  write path importFrom always had.
- 🩺 **New diagnostics check (32 total)**: **shortcut template
  validity** — expandTemplate keeps unknown {variables} literal, so a
  typo like {Datee} silently committed garbage on every expansion; the
  check validates every well-formed variable against the known set
  (case-insensitive) and passes when no templates exist at all.
- 📤 **Diagnostic report export, finally**: the renderReport() renderer
  existed complete since V 1.0.6 with ZERO callers — an "Export report"
  button in the event-log card now writes it (version + check summary +
  events) through the system file picker.
- 🌐 **50 new AR/EN string keys**, parity-checked automatically
  (PARITY OK — 2105 keys per language).
- ✅ **198 unit tests passing** (was 179), covering every new
  operation's edge cases, the five-hex-digit regex trap (`\uXXXX` is
  exactly four digits — caught by the emoji test on the first run and
  fixed with `\x{...}`), the pin tool from code to registry to internal
  keys and the extended tail contract, both new scale bounds, the
  readable-color helper, the context pipeline through merge/sanity/
  smuggled-key rejection, the backup preview's real counts, and the
  template check's accept/reject/silent examples.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.7.0.apk` — install directly (safe
  in-place upgrade over any previous release, same signing key)
- **AAB**: for advanced needs
- **SHA256SUMS.txt**: full cryptographic verification of every asset
- **Theme packs** (8): optional add-on themes, installable later

## Privacy

Everything runs **fully offline**: no internet on the typing path at all.
Statistics are anonymous counters on your device (never text, never
per-keystroke timestamps, never field identity — context modes are just a
DETECTED attribute of the focused field), and backups are local JSON files
whose destination you pick through the system file picker — now with a
confirm preview before anything is replaced.

</div>
