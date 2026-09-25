<div align="center">

# DRS Smart Keyboard V 1.20.0

## الجولة الشاملة العشرون — الدقة والإتقان: الفحص الثلاثي أثمر — لوحة الأرقام تخرج من الحبس، خمسة إصلاحات جوهرية في التزلج، اقتراحات أعدل، خصوصية أقسى، ونسخ احتياطي لا يفشل

**Twentieth Comprehensive Round — Precision & Mastery: The Tri-Front Audit Pays Off — The Trapped Number Pad Freed, Five Core Glide Fixes, Fairer Suggestions, Tougher Privacy, and a Backup That No Longer Fails**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.20.0 — جولة الدقة والإتقان

بعد جولة الفحص الشامل في v1.19.0، جاءت هذه الجولة لتجني ثماره: فحصٌ ثلاثي
الجبهات جديد كشف زلاتٍ دقيقة كان الفحص السابق لا يراها — فُصلت كل زلة إلى
إصلاح حقيقي مُختبر، لا لمسة تجميل.

- 🔓 **لوحة الأرقام تخرج من الحبس — أخيرًا** — أي حقل رقمي في أي تطبيق
  كان يقفلك على لوحة 4×4 صمّاء: أرقام وفاصلة ونقطة فقط، **بلا أي مفتاح
  للعودة إلى الحروف** — لا مسافة ذكية ولا مفتاح ABC. الآن تحمل كلتا
  لوحتي الأرقام (العادية وPC) مفتاح **«الحروف»** في الصف الأخير يعيدك
  إلى لوحة الحروف بضغطة، تمامًا كما تفعل أقوى لوحات المفاتيح عالميًا.
  وعُقدة اختبار جديدة تضمن أن كل لوحة غير حرفية تحتوي مفتاح خروج —
  فلن يعود الحبس إلى إصدار صامت أبدًا.

- 📞 **لوحة الهاتف مكتملة** — الفاصلة «,» متاحة بضغط مطول على «.»،
  ومفتاح الانتظار «;» (المعيار الهاتفي لتعليق الاتصال) بضغط مطول على
  «-»، وفي لوحة الهاتف الثانية زال التكرار العبثي: كان فيها مفتاحان
  يفعلان الشيء نفسه حرفيًا («pause» والفاصلة كلاهما يرسل «,») — بقي
  مفتاح واحد ومُلئت الفجوة بفاصل الوقت «:» المفيد لمراسلات المواعيد.

- ⛸️ **خمسة إصلاحات جوهرية في التزلج (الكتابة بالسحب)** —
  1. **لا مزيد من تجميد الواجهة**: تحميل قاموس 50 ألف كلمة وبناء
     فهرس القاطع كانا يجريان **بشكل متزامن على خيط الواجهة** عند أول
     إظهار لكل لغة (ومع كل تغيير يفرغ الكاش) — انتقلا إلى الخلفية
     مع بوابة الجاهزية القائمة فلا تتجمد اللوحة ولا تثير مراقبات ANR.
  2. **المعاينة لم تعد تُبتلع**: أثناء التزلج كانت رقاقة الحافظة
     تخطف صف المرشحين وتخفي معاينة الكلمة إذا كان لديك أي مقطع حديث
     منسوخ (أشيع حالة على الإطلاق: نسخ ثم تزلج!) — الآن المعاينة
     الحية لها الأولوية ما دامت الإيماءة جارية، ويتولى الاقتراح
     الاعتيادي زمامه فور عودة الكتابة الحقيقية.
  3. **لا مخزن متضخم ولا حالة عالقة**: كان أثر الإصبع يتراكم في
     الذاكرة **بلا حدود طوال عمر العملية** وإشارة «أنا أتزلج الآن»
     تبقى مرفوعة إلى الأبد متى كان إظهار الأثر معطلًا — لأن المسح
     كله كان داخل بوابة ترفيهية. المسح الآن دائم والبوابة للأنيماشن
     فقط.
  4. **عدالة القاطع للكلمات المكررة الحروف**: كاش الأطوال المثالية
     كان يفتح بالمفردة وحدها بينما لكل كلمة متكررة الحروف (pool،
     letter) متغيرا إيماءة — الطبيعي والحلزوني — فكان الطويل
     يُحاكم بطول القصير ويُقصى خطأً. المفتاح الآن بالمفردة والمتغير
     معًا.
  5. **كاشف أعدل للبدايات البطيئة**: كان السحب يُحكم عليه من السرعة
     المتوسطة منذ لحظة اللمس ويُقفل «ليس تزلجًا» نهائيًا بعد 500
     ميلي ثانية — فالضغط ثم التوقف ثم الانطلاق كان محرومًا من التزلج
     كليًا. صار القياس **بنافذة انزلاقية** على أحدث ~500 ميلي ثانية
     من الحركة (مثل Gboard)، مع حارس مؤشر صحيح (`findPointerIndex`
     بدل `actionIndex` الذي يعني دائمًا الصفر في الحركة فكان يهمش
     أي إصبع غير الأول)، وقفل الضغط المطول أصبح يفحص **كل** الأصابع
     لا الإصبع صفر حصرًا.

- 🎯 **اقتراحات أعدل — والتعلم يقبل «لا»** —
  - **نهاية قص البادئات المعجمي**: البحث عن مرشحي البادئة كان يقفز
    أول 48 نتيجة معجمية **ثم** يرتبها بالتكرار — فالبادئات المنتجة
    («ال»، «al») كانت تفقد أكثر كلماتها تكرارًا ببساطة لأنها وُلدت
    بعد الجولة. محرك جديد (`findTopByPrefix`) يمسح المدى الكامل
    بكومة صغرى مقيّدة فيختار الأشيع فعلًا.
  - **كلماتك تُطابق كما تنطقها**: الكلمات المتعلمة باللاتينية كانت
    تُقارن بلا طيّ العلامات — «élève» المتعلمة لم يكن «eleve»
    يصل إليها قط رغم أن كلمات القاموس نفسها تُطابَق بالطيّ. صار
    الخطّان خطًا واحدًا، في المحرك وفي المدقق الإملائي معًا.
  - **حالة الكلمة التالية واعية بالجملة**: التنبؤ بالكلمة التالية
    كان يقلّد حالة الكلمة السابقة نفسها — «Hello » كان يكبّر التالية
    و«Hi. » لا يكبّر! الصحيح عكس ذلك تمامًا: بعد نهاية الجملة (. !
    ? … ؟ ۔) تُكبّر، وبعد كلمة عادية تبقى كما هي.
  - **الرفض يُعلّم أيضًا**: رجوعك عن تصحيح تلقائي كان يترك الكلمة
    المرفوضة جالسة في قاموسك الشخصي بكامل قوتها إلى الأبد. الآن كل
    رفض يصرّف 64 نقطة من تكرارها ويحذفها كليًا عند نزولها للصفر.
  - **المدخلات العالمية تظهر أخيرًا**: الكلمات المستوردة «لكل اللغات»
    (بلا لغة) كانت موجودة في القاعدة لكن استعلام الاقتراحات لا يعيدها
    أبدًا رغم أن توثيق الدالة يدّعي ذلك حرفيًا — الاستعلام صار صادقًا،
    في القاموس الداخلي وقاموس النظام معًا.
  - **الإيموجي بلا خسائر صامتة**: الفلترة كانت بعد القص، فأي مرشحين
    صفرّيي الوزن في القمة يجعل النتائج أقل من المطلوب رغم وجود بدائل
    حقيقية. الفلترة صارت قبل القص.

- 🔒 **خصوصية أقسى: كلمات المرور تخفي تلقائيًا** — حقول كلمات المرور
  كانت تعطل التركيب فقط، ولو لم يعلن التطبيق المضيف راية
  «بلا تعلم شخصي» بقيت لوحتنا تتعلم داخل حقل كلمة السر! الآن كل حقل
  كلمة مرور يفرض التخفي الكامل (بلا تعلم، بلا سجل حافظة) كما تفعل
  Gboard وSwiftKey — حتى لو لم يطلب المضيف ذلك.

- 💾 **نسخ احتياطي لا يفشل ولا يجمّد** — ثلاث زلات حقيقية في شاشتي
  النسخ والاستعادة: (1) الكتابة والفك كانتا على الخيط الرئيسي — مئات
  الميغابايتات من وسائط الحافظة = تجميد وANR مؤكد، انتقل كل IO إلى
  `Dispatchers.IO`؛ (2) مقبض مساحة العمل كان متغيرًا محليًا عاديًا،
  فتغيير تكوين أثناء فتح منتقي الملفات يفقده ويجعل النقر ينهار
  بـ`!!` — يظهر للمستخدم كـ«فشل النسخ» زائفًا — صار محفوظًا عبر
  التكوينات كنظيره في الاستعادة؛ (3) عنصر وسائط واحد بمعرف مكسور
  كان يُسقط النسخ أو الاستعادة **كلها** في منتصف الطريق — الآن
  يُتخطى العنصر المعطوب وتُكمل العملية.

- 🏗️ **CI أقوى: بوابات حقيقية** — بناء الإصدار الموقّع كان ينشر
  بلا تشغيل اختبارات ولا تحقق من الإصدار. الآن ينهار الإصدار فورًا
  إن لم تطابق `gradle.properties` وسم الإصدار، ولا يُبنى APK قبل
  نجاح **492 اختبار وحدة** كاملة.

- 🧹 **نظافة ممتدة** — أربعة إعدادات ميتة حُذفت من الجذر (نمط شعر
  الإيموجي المفضل — يُشتق من المحارف أصلًا، علامة بيتا داخلية بلا
  قارئ، ووجهتا «جهات الاتصال» و«إدخالات قاموس المستخدم» اللتان لم
  يُنفذ وعدُهما قط) مع سلاسلها من **44 لغة** كي لا يبقى ترجمة يتيمة؛
  توستا القص والنسخ المضمنان إنجليزيًا (منذ اليوم الأول بخطأ إملائي
  «Eiter») صارا مفتاحين معرّبين؛ توست «لا لوحة صوتية» عُرّب؛ وبلاطتا
  العنصر النائب NOOP/DRAG_MARKER في محرر الأفعال لم تعد تمر عبر مسار
  «مفتاح مجهول» — التوازي الآن **2,374 مفتاحًا لكل لغة** بالضبط.

- 🧪 **20 اختبار وحدة جديدًا (492 ناجحة، كانت 472)** — عقود
  `findTopByPrefix` (الترتيب بالتكرار على كامل المدى، الحد، الحواف)،
  عقود `findByPrefix` الأصلية غير المتأثرة، حالة الكلمة التالية
  بالإنهامات العربية واللاتينية، قرار بدء الإيماءة بالنافذة
  الانزلاقية (السرعة، المسافة، استثناء مفاتيح السحب، قسر الصفر)،
  أسبقية معاينة التزلج على رقاقة الحافظة بحالاتها الأربع، حساب
  الصرف عند الرفض حتى الحذف، و**عقود بنية اللوحات نفسها**: لوحتا
  الأرقام بمفتاح خروج، لوحة الهاتف بفواصلها المطولة، ولوحة الهاتف
  الثانية بلا تكرار — فتصبح البيانات التي تخدم أصابعك محروسة
  كالكود.

</div>

<div dir="ltr">

## About the project

**DRS Smart Keyboard** is a free, open-source Android keyboard
(Apache-2.0) built with Kotlin, Jetpack Compose and Material 3 —
**Arabic-first**: a fully Arabic interface with native RTL support,
thoughtful Arabic layouts and Arabic suggestions/correction, with English
as a complete second option. Everything stays on your device: no accounts,
no tracking, no ads.

## What's new in V 1.20.0 — The Precision & Mastery Round

After v1.19.0's full audit, this round harvests the next layer: a fresh
tri-front audit uncovered subtle defects the previous pass couldn't see —
every one of them fixed with a real, tested change.

- 🔓 **The number pad is finally free** — any numeric field in any app
  used to lock you onto a silent 4×4 pad with **no way back to letters**
  — no ABC key, nothing. Both number pads (standard and PC layout) now
  carry a **"characters"** exit key on the bottom row, exactly like the
  world's leading keyboards. A new test contract guarantees every
  non-letter layout contains an exit key, so the trap can never silently
  return.

- 📞 **The phone pad is complete** — comma is one long-press away on
  ".", the telephony wait separator ";" (used to pause call strings) is
  a long-press on "-", and the second phone pad lost its pointless
  duplication (a "pause" key and a comma key that literally inserted the
  same character) — the freed slot now holds the time separator ":".

- ⛸️ **Five core glide-typing fixes** —
  1. **No more UI freezes**: loading the 50k-word dictionary and building
     the pruner index used to run **synchronously on the composition
     thread** on first layout per language — both moved to a background
     scope behind the existing readiness gate.
  2. **The preview is no longer swallowed**: during a glide, the clipboard
     chip hijacked the candidate row whenever any recent clip existed (the
     most common state: copy, then glide!) — the live word preview now wins
     while the gesture is active, and normal suggestions take over the
     moment real typing resumes.
  3. **No unbounded buffer, no stuck state**: with the trail setting off,
     every gesture kept accumulating points in memory for the whole process
     lifetime and `isGliding` stayed true forever — the buffer clear and
     state reset are now unconditional; only the cosmetic fade stays gated.
  4. **Fair pruning for double-letter words**: the ideal-length cache was
     keyed by word only while repeated-letter words (pool, letter) produce
     TWO gesture variants — the loop variant was judged against the plain
     variant's cached length and mis-pruned. The key is now word AND variant.
  5. **A fairer detector for slow starts**: glide was decided from average
     velocity since touch-down and permanently latched "not a gesture" after
     500 ms — press, dwell, then glide was impossible. Measurement now uses
     a **sliding window** over the latest ~500 ms of movement (Gboard-style),
     with a correct pointer guard (`findPointerIndex` instead of an
     `actionIndex` that is always 0 during moves) and long-press blocking
     that considers **every** finger, not hardcoded pointer zero.

- 🎯 **Fairer suggestions — and learning that accepts "no"** —
  - **Prefix ranking over the full range**: candidates used to cut the
    first 48 lexicographic hits and only then sort by frequency — productive
    prefixes ("ال", "al") silently lost their most frequent words. A new
    engine (`findTopByPrefix`) scans the entire matching range with a bounded
    min-heap.
  - **Your words match as you type them**: Latin user-dictionary words were
    compared without accent folding — a learned "élève" was unreachable from
    "eleve" even though dictionary words fold. Both sides now share one
    pipeline, in the engine and in the spell checker.
  - **Sentence-aware next-word casing**: next-word predictions used to copy
    the previous word's own case — "Hello " capitalized the next word while
    "Hi. " didn't. Exactly backwards: after a sentence terminator
    (. ! ? … ؟ ۔) the prediction is capitalized, after a plain word it isn't.
  - **Reverted suggestions unlearn**: rejecting an auto-correct used to
    leave the unwanted word in your personal dictionary at full strength
    forever. Every revert now drains 64 frequency points and deletes the
    entry entirely when it reaches zero.
  - **Global dictionary entries finally surface**: words imported for "all
    languages" (null locale) sat in the database but the suggestion query
    never returned them despite the function's own documentation claiming it
    does — both the private Room DAO and the system-provider DAO now match
    the documented contract.
  - **Emoji without silent losses**: zero-weight matches were cut after the
    limit, shrinking result counts below the cap when real matches existed
    further down — filter now runs before the limit.

- 🔒 **Tougher privacy: password fields go incognito automatically** —
  password fields only used to disable composing; if the host app didn't
  advertise IME_FLAG_NO_PERSONALIZED_LEARNING, the keyboard kept learning
  inside a password field. Every password field now forces full incognito
  (no learning, no clipboard history), matching Gboard/SwiftKey behavior.

- 💾 **A backup that neither fails nor freezes** — three real defects in
  the backup/restore screens: (1) archive writing and unzipping ran on the
  main thread — hundreds of MB of clipboard media meant guaranteed jank and
  ANR; all of it moved to `Dispatchers.IO`; (2) the backup workspace handle
  was a plain local var, so a config change while the file picker was open
  lost it and the callbacks crashed on `!!` — surfacing as a bogus "backup
  failed"; it is now remember-backed like its restore counterpart; (3) a
  single media item with a broken URI aborted the whole backup or restore
  midway — broken items are now skipped and the operation completes.

- 🏗️ **Stronger CI: real gates** — signed releases were published without
  running tests and without verifying the version. A release now fails fast
  if `gradle.properties` doesn't match the pushed tag, and no APK is built
  until all **492 unit tests** pass.

- 🧹 **Extended hygiene** — four dead settings deleted at the root
  (preferred emoji hair style — derived from code points anyway, an
  internal beta flag with zero readers, and the never-implemented contacts /
  user-dictionary-entries spell switches) together with their strings from
  **44 locales** so no orphan translations remain; the hardcoded English
  cut/copy toasts (carrying the "Eiter" typo since day one) became localized
  keys; the "no voice IME found" toast is localized; and the NOOP/
  DRAG_MARKER placeholder tiles in the actions editor no longer trip the
  "unknown key" error path. Exact parity: **2,374 keys per language**.

- 🧪 **20 new unit tests (492 passing, up from 472)** — findTopByPrefix
  contracts (frequency ranking over the full range, limits, edges), the
  untouched findByPrefix contract, next-word casing with Latin and Arabic
  terminators, the sliding-window gesture-start decision (velocity, travel,
  swipe-key exclusion, zero-duration clamp), glide-preview-over-clipboard
  precedence in all four states, revert demotion down to deletion, and —
  new this round — **contracts over the layout JSON themselves**: both
  number pads expose an exit, the phone pad exposes its long-press
  separators, and the second phone pad has no duplicated keys.

## Install

1. Download `DRS-Smart-Keyboard-v1.20.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Read the full `PRIVACY.md`.

</div>
