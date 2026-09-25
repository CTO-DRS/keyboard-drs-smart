<div align="center">

# DRS Smart Keyboard V 1.17.0

## الجولة الشاملة السابعة عشرة — جودة عالمية: التصحيح التلقائي الحقيقي، لوحة تكلّم أخيرًا، وحذف الحركة أولًا

**Seventeenth Comprehensive Round — World-Class Quality: True Autocorrect, a Keyboard That Finally Speaks, and Haraka-First Backspace**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.17.0 — جودة عالمية

- ✅ **التصحيح التلقائي الحقيقي** — حتى اليوم لم يكن أي مرشّح مؤهلًا للإرسال
  التلقائي أبدًا، فالمسافة كانت تُثبت الخطأ المطبعي كما كُتب. أصبح الآن
  **مُصحِّح فعلي محافظ**: حين لا يبدأ أي كلمة في القاموس بما كتبته (أي أن
  الكلمة خطأ حقيقي لا مجرد صيغة نادرة)، وكانت الكلمة من ثلاثة أحرف فأكثر،
  وكان أقرب تصحيح بكلفة تحرير واحد من كلمات رأس القاموس شائعة الاستخدام،
  والخيار مُفعّل — تُصلح المسافة الكلمة صمتًا، **والمسح للخلف يتراجع**
  (عقد التراجع موجود من قبل). مفتاح «التصحيح التلقائي» في الشريط صار
  يقلب هذا السلوك الحقيقي بدل إخفاء صف الاقتراحات، ومفتاح Switch جديد في
  إعدادات الكتابة، والحماية تشمل كلمات المستخدم الشخصية (لا تُصحَّح أبدًا).

- ♿ **الوصول الشامل — لوحة المفاتيح تتكلم أخيرًا** — كانت المفاتيح صناديق
  بلا هوية بالنسبة لـ TalkBack (الاكتشاف باللمس لا ينطق شيئًا). الآن كل
  مفتاح يعلن عن نفسه: مفاتيح الأيقونات (التحويل، التثبيت، الحذف، الأسهم،
  الإدخال، تنقل السطور، مفاتيح اللوحات بما فيها لوحة الحركات) تحصل على
  تسميات منطوقة محلية عبر دالة نقيّة مختبرة، ومفاتيح الحروف تنطق حرفها،
  وبلاطات الإيموجي تنطق الإيموجي نفسه، وبلاطات الحافظة تعلن «صورة/فيديو/
  مثبت» بدل الصمت. سبعة عشر مفتاح سلاسل وصول جديدًا.

- ⌫ **حذف الحركة أولًا** — كان الحذف يزيل الحرف وحركاته كلها في نقرة
  واحدة (تجميعات ICU الصوتية). صار الآن — بمفتاح افتراضي جديد — ينزع
  **حركة واحدة في كل نقرة**: شدة ثم فتحة ثم الحرف، تحكم كامل فوق التكديس
  الذكي الذي تنتجه لوحة الحركات. خامس عشر دالة نقية في محرك الحركات.

- ⚡ **أداء المحرر الضخم** — البحث في نصوص حتى 500,000 حرف كان يُعاد
  مزامنةً على خيط الواجهة مع كل ضغطة. نُقل إلى خيط الخلفية بتأخير قصير
  (150 مللي ثانية) **في النافذتين معًا** (المنبثقة وداخل اللوحة).

- 🧼 **نظافة المنصة** — StrictMode (خيوط + آلة افتراضية) في بناء التطوير
  فقط؛ حذف مجلدي لغة فارغين (أوردو بصفر ترجمة، والألمانية السفلى بمفتاحين)
  من الموارد ومن `localeConfig` (44 ← 42) فلا يعرض النظام لغات بلا ترجمة؛
  `CHANGELOG.md` مولّد من وسوم الإصدارات الستة والعشرين.

## التثبيت

1. حمّل ملف `DRS-Smart-Keyboard-v1.17.0.apk` من الأسفل.
2. ثبّته (اسمح بالتثبيت من مصادر غير معروفة عند الحاجة).
3. افتح الإعدادات ← أنظمة ← اللغات وأدخل لوحة المفاتيح ← فعّلها.
4. اخترها لوحة مفاتيح افتراضية وابدأ الكتابة.

التحقق من سلامة الملف: قارن بصمة SHA-256 في `SHA256SUMS.txt` المرفق.

## الخصوصية

كل شيء يعمل **محليًا وبلا إنترنت**: لا حسابات، لا تتبع، لا إعلانات، لا
شبكة إلا لفحص تحديثات التطبيق نفسه. اقرأ `PRIVACY.md` الكامل.

</div>

<div align="center">

---

## What's New in V 1.17.0 — World-Class Quality

- ✅ **TRUE autocorrect** — no candidate was ever auto-commit eligible
  before, so space always committed typos verbatim. A conservative pure
  decider now gates the existing auto-commit plumbing (no dictionary
  prefix match = a genuine typo, ≥ 3 letters, top correction frequency
  ≥ 170/255, pref on); space silently fixes the word and **backspace
  reverts**. The toolbar autocorrect toggle now flips this real behavior
  (not the suggestion row), with a new switch in Typing settings.
- ♿ **Accessibility — the keyboard finally speaks** — keys were unlabeled
  boxes to TalkBack. Every key now announces itself: icon-only keys get
  localized spoken labels through a tested pure mapping (shift, caps
  lock, delete, arrows, enter, line moves, view switches including the
  harakat page), character keys speak their letter, emoji tiles announce
  the emoji, and clipboard media tiles say image/video/pinned.
- ⌫ **Haraka-first backspace** — delete used to remove the letter with
  all its marks in one tap (ICU grapheme clusters). A new default-on
  setting peels **one diacritic per tap** (shadda, then the haraka, then
  the letter) for full control over smart mark stacking.
- ⚡ **Huge-text editor performance** — search across up to 500,000 chars
  moved off the main thread behind a 150 ms debounce in BOTH editors.
- 🧼 **Platform hygiene** — debug-only StrictMode; removed the empty
  Urdu and 2-key Low German locale stubs from resources and localeConfig
  (44 → 42 real languages); CHANGELOG.md generated from all 26 release
  tags.

## Install

1. Download `DRS-Smart-Keyboard-v1.17.0.apk` below.
2. Install it (allow unknown sources if prompted).
3. Settings → Systems → Languages & input → enable DRS Smart Keyboard.
4. Pick it as your default keyboard and type.

Verify integrity with the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no
ads, no network except checking for app updates. See `PRIVACY.md`.

---

**License**: Apache-2.0 · **Kotlin** · **Jetpack Compose** · **Material 3**

</div>
