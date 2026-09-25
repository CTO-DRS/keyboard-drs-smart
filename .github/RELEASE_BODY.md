<div align="center">

# DRS Smart Keyboard V 1.18.0

## الجولة الشاملة الثامنة عشرة — قوة عالمية: R8 مفعّل بأمان مُثبَت، لوحة مفاتيح مقسومة، قواميس ثماني لغات جديدة، وترجمة إسبانية موسعة

**Eighteenth Comprehensive Round — Global Strength: R8 Re-Enabled with Proven Safety, a Split Keyboard, Eight New Language Dictionaries, and an Expanded Spanish Translation**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.18.0 — قوة عالمية

- 🗜️ **R8 مفعّل أخيرًا — ببوابة أمان مُثبَتة** — منذ الإصدار v1.7 كان
  التصغير معطلًا بسبب فشل «الشاشة الفارغة» الشهير: dex بحجم 3.1 ميغابايت
  بلا أي واجهة. أُعيد التحقق تجريبيًا: **الفشل لم يعد يُستنسخ** مع سلسلة
  الأدوات الحالية (تجربة مضبوطة مع إبقاء نقاط الدخول صراحةً وبدونها أنتجت
  dex كاملًا في الحالتين: 6.8 ألف فئة، كل شاشات Compose وmaterial3 ووقت
  تشغيل JetPref ونماذج التفضيلات المولّدة كلها موجودة). بقيت قواعد إبقاء
  نقاط الدخول الأربع كتأمين إضافي، وأُضيفت **بوابة `validateR8Dex`**
  تفحص الـdex الناتج بايتًا بايتًا بعد كل بناء (beta وrelease) وترفض
  البناء فورًا إذا اختفت أي فئة علامة — فلن يصل فشل «الشاشة الفارغة» إلى
  إصدار صامتًا مرة أخرى. النتيجة: dex أنضف وموارد مصغّرة (`shrinkResources`
  مفعّل) وسجل انهيارات مقروء كما هو (`-dontobfuscate`).

- ⌨️ **لوحة المفاتيح المقسومة** — صفحة الحروف تُرسم في نصفين بفجوة وسطى
  حقيقية داخل محرك التخطيط نفسه (تُحسب في فرعي الاتساع والتقليص معًا
  فتتماشى مع كل أوزان المفاتيح والهوامش). ثلاثة أوضاع في إعدادات لوحة
  المفاتيح: **تلقائي** (على العريض ≥560dp فقط: الهاتف بالوضع الأفقي،
  الأجهزة القابلة للطي، اللوحية — الافتراضي)، **دائمًا**، **أبدًا**.
  لا تُفعَّل في نافذة اليد الواحدة ولا النافذة العائمة ولا على لوحات
  الأرقام والهاتف — فقط حيث تنفع فعلًا. محرك القرار نقية بالكامل و21
  اختبارًا جديدًا.

- 🌍 **قواميس ثماني لغات جديدة — لا اقتراحات إنجليزية بالوكالة** — كانت
  كل لغة عدا العربية تحصل على قاموس الإنجليزية العام (فرنسي يكتب
  فيتلقى اقتراحات إنجليزية!). أضفنا قواميس تكرارية مُنتقاة بعناية
  لـ **الفرنسية والألمانية والإسبانية والإيطالية والبرتغالية والتركية
  والروسية والفارسية** (~8 آلاف مدخل بمجموع منحنى تقييم تنازلي يحترم
  عتبة التصحيح التلقائي)، مع **تطوي حروف اللاتينية** للمطابقة فقط:
  اكتب «eleve» فتظهر «élève»، و«größe» تعنيه المطابقة «grosse»،
  والهمزات العربية الموحّدة كما هي مختبرة. واللغات بلا قاموس تتراجع
  للإنجليزية تراجعًا صادقًا معلنًا، وجداول الأزواج الثنائية (التنبؤ
  بالكلمة التالية) بقيت للعربية والإنجليزية بلا تزييف.

- 🇪🇸 **ترجمة إسبانية موسعة** — 277 مفتاحًا جديدًا يغطي بلاطات الحافظة
  ومحررها الذكي كاملًا (البحث والاستبدال والخوارزميات والخطوط والنتائج
  الملونة وكشف الكود وإعداداتها الست مجموعات) ولوحات الحركات والرموز
  والحواف الذكية ومفاتيح الوصول (TalkBack) والأفعال السريعة — وصل تغطية
  الواجهة الإسبانية إلى ~1,256 مفتاحًا، وبقية شاشات التشخيص والإحصاء
  تكتمل في الجولة القادمة.

- 🐛 **إصلاح جوهري: 24 مفتاحًا إنجليزيًا كانت عربية!** — فحص التوازي
  كشف أن مفاتيح v1.17 الوصولية وإعدادات التصحيح والحذف الأولى للحركة
  دخلت النص العربي إلى ملف الإنجليزية نفسه (كان TalkBack ينطق تسميات
  عربية لمستخدم الإنجليزية). صُححت كلها بترجمات إنجليزية سليمة مع
  الحفاظ على توازي 2,348 مفتاحًا لكل لغة حرفيًا.

## التثبيت

1. حمّل ملف `DRS-Smart-Keyboard-v1.18.0.apk` من الأسفل.
2. ثبّته (اسمح بالتثبيت من مصادر غير معروفة عند الحاجة).
3. افتح الإعدادات ← أنظمة ← اللغات وأدخل لوحة المفاتيح ← فعّلها.
4. اخترها لوحة مفاتيح افتراضية وابدأ الكتابة.

التحقق من سلامة الملف: قارن بصمة SHA-256 في `SHA256SUMS.txt` المرفق.

## الخصوصية

كل شيء يعمل **محليًا وبلا إنترنت**: لا حسابات، لا تتبع، لا إعلانات، لا
شبكة إلا لفحص تحديثات التطبيق نفسه. اقرأ `PRIVACY.md` الكامل.

</div>

<div dir="ltr">

## About the project

**DRS Smart Keyboard** is a free, open-source (Apache-2.0) Android keyboard
built with Kotlin, Jetpack Compose and Material 3, designed **Arabic-first**
with a fully localized RTL interface and thoughtful Arabic layouts, plus
English as a complete second option. Everything you type stays on your
device: no accounts, no tracking, no ads.

## What's new in V 1.18.0 — Global Strength

- 🗜️ **R8 finally enabled — with a proven safety gate** — minification had
  been disabled since v1.7 because of the infamous "blank screen" failure
  (a 3.1MB dex with no UI). A controlled experiment proves the failure no
  longer reproduces on the current toolchain: minifying with and without
  explicit entry-point keeps both produced a complete dex (6.8k+ classes;
  every Compose screen, material3, the JetPref runtime and the generated
  preference models all present). Explicit entry-point keeps stay as
  belt-and-suspenders, and a new **`validateR8Dex` gate** byte-scans the
  minified dex after every beta/release build and fails the build the
  moment any marker class is missing — a blank-screen regression can never
  reach a release silently again. Resources shrink too; stack traces stay
  readable (`-dontobfuscate`).

- ⌨️ **Split keyboard** — the letters page renders as two halves with a
  real central gap computed inside the layout engine itself (in both the
  grow and the shrink branch, so it composes with every width factor and
  margin rule). Three modes in Keyboard settings: **Auto** (wide screens
  ≥560dp only: landscape phones, foldables, tablets — the default),
  **Always**, **Never**. It never engages in one-handed or floating
  windows, nor on the numeric/phone pads — only where it actually helps.
  The decision engine is fully pure, with 21 new tests.

- 🌍 **Eight new language dictionaries — no more English-by-proxy** —
  every non-Arabic language used to receive the generic ENGLISH dictionary
  (a French typist got English suggestions!). We added carefully curated
  frequency dictionaries for **French, German, Spanish, Italian,
  Portuguese, Turkish, Russian and Persian** (~8k entries with a rank-based
  descending score curve that respects the autocorrect threshold), plus
  **Latin accent folding for matching only**: type "eleve" and "élève"
  appears, "größe" matches "grosse", while the Arabic hamza unification is
  pinned by tests. Languages without a bundled dictionary fall back to
  English honestly and openly, and next-word bigram tables remain where
  they truly exist (Arabic, English).

- 🇪🇸 **Expanded Spanish translation** — 277 new keys covering the
  clipboard tiles and its full smart editor (search/replace, algorithms,
  fonts, colored results, code detection and its six settings groups),
  the harakat/symbols/letters panels, accessibility (TalkBack) keys and
  quick actions — bringing Spanish UI coverage to ~1,256 keys. The
  remaining diagnostics/stats screens complete next round.

- 🐛 **Root-cause fix: 24 English keys carried Arabic text** — the parity
  audit revealed v1.17's accessibility and autocorrect/backspace-preference
  keys had landed in the ENGLISH file as Arabic text (TalkBack spoke
  Arabic labels to English users). All 24 fixed with proper English,
  preserving exact 2,348-key per-language parity.

## Install

1. Download `DRS-Smart-Keyboard-v1.18.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Read the full `PRIVACY.md`.

</div>
