# DRS Smart Keyboard — V 1.16.0 (versionCode 28)

## الجولة الشاملة السادسة عشرة: لوحة الحركات كلوحة مفاتيح كاملة + شريط المهام الثابت بعشر خانات + الترتيب الذكي للوحات

**طلب المستخدم الصريح:**
- «انا طلبت لوحة مفاتيح مشابه تماماً للوحة الحروف او الارقام ولكنها عباره عن حركات»
- «وايضآ شريط المهام اجعله ثابتآ يعرض 10مهام فقط مع امكانية تغيرها وتعديلها»
- «واعد ترتيب وتتطوير جميع الوحات بنظام مرتب وذكي»

---

## العربية

### ١) لوحة الحركات كلوحة مفاتيح كاملة — «مشابهة تمامًا للوحة الحروف أو الأرقام»
أُعيد بناء لوحة الحركات من شبكة بلاطات صغيرة إلى **لوحة مفاتيح حقيقية بثلاثة تشريح لوحة الأرقام نفسه: أربعة صفوف × أربعة مفاتيح عريضة**، مرسومة عبر **العنصر المرئي نفسه (DrsImeUi.Key) وبارتفاع الصف والهوامش نفسهما** الذي ترسم به لوحة الحروف والأرقام مفاتيحها — فالشكل والملمس وحالة الضغط مطابقة تمامًا:

- **الصف الأول:** تنوين الفتح ً، تنوين الضم ٌ، تنوين الكسر ٍ، الألف الخنجرية ٰ
- **الصف الثاني:** الفتحة َ، الضمة ُ، الكسرة ِ، السكون ْ
- **الصف الثالث:** الشدة ّ، التطويل ـ، **مفتاح الحذف الحقيقي ⌫ (يكرر الحذف مع الاستمرار بالضغط)**، **مفتاح المسافة الحقيقي**
- **الصف الرابع:** التشكيل المزدوج َّ ُّ ِّ ّْ (محرفان بنقرة واحدة)
- الحروف المركبة تُعرض فوق **الدائرة المنقطة ◌** كما في لوحات المفاتيح العربية الحقيقية
- كل الأنظمة الذكية بقيت: **الدمج الذكي** (حركة فوق حركة تستبدلها بدل التراكم، والشدة تلحق بالحركة)، شريط **الأكثر استخدامًا** فوق المفاتيح، وأداة **إزالة التشكيل** في الرأس

### ٢) شريط المهام الثابت — «يعرض 10 مهام فقط مع إمكانية تغييرها وتعديلها»
- الشريط فوق شريط الاقتراحات صار **ثابتًا: عشر خانات دائمًا بلا تمرير** — تثبياتك أولًا بترتيبها، ثم تُستكمل الخانات من الافتراضية والكتالوج المرئي حتى العشر
- **تغييرها:** **اضغط مطولًا على أي خانة** فيفتح محرر الخانة الجديد فوق منطقة الكيبورد — اختر مهمة من الكتالوج (مجمّع بالأدوات/التحرير/المؤشر) لتشغل الخانة نفسها مكان القديمة، والاستبدال يبقى في مكانه حتى بعد إعادة التشغيل
- **تعديلها:** درج المهام (زر السحب الجانبي) يبقى مركز الترتيب (أعلى/أسفل) والتثبيت والإزالة
- مفاتيح المستوى التقني (المتقدم/المزدوج) تبقى في ذيل الشريط — فهي مفاتيح لا مهام، والعشر خانات عشر دائمًا
- مبدّل المستوى (بسيط/تقني/مزدوج) لنظام «كلاهما» انتقل إلى الدرج

### ٣) «أعد ترتيب وتطوير جميع الوحات بنظام مرتب وذكي»
- **الترتيب الذكي:** رقاقات مبدّل اللوحات (حركات/رموز/حروف) **تُرتَّب تلقائيًا من عدّادات فتلك اللوحات المحلية** — اللوحة الحالية أولًا ثم الأكثر فتحًا، وكسر التعادل بترتيب الكتالوج — مع مفتاح جديد في الإعدادات (اللوحات الذكية → الترتيب الذكي للوحات)
- كل لوحة تسجّل فتحها **محليًا فقط** (لا نصوص ولا طوابع زمنية) **والوضع الخفي لا يسجّل شيئًا إطلاقًا**
- لوحة الرموز الذكية ولوحة الحروف الموسعة تشاركان نفس التشريح والرأس والمبدّل الذكي

**الاختبارات:** 394 اختبار وحدة ناجحًا (كانت 373) — 21 اختبارًا جديدًا تغطي الخانات العشر الثابتة (ترتيب التثبيات، ملء الافتراضيات، حشو الكتالوج، منع التكرار، السقف)، استبدال الخانة (الاستبدال الدقيق، إزالة التكرار، رفض الفهرس الخاطئ والمعرف المجهول)، الترتيب الذكي (الحالية أولًا، حسب الاستخدام، كسر التعادل، العدادات المفقودة، لوحة واحدة، تسجيل الفتح)، وتشريح لوحة الحركات (4×4، جميع العلامات، صف التركيبات، تسميات الدائرة المنقطة، سلامة محرك الدمج الذكي).

**السلاسل:** 7 مفاتيح AR/EN جديدة (توازي 2317 لكل لغة).

---

## English

### 1) The harakat panel rebuilt as a REAL keyboard — «exactly like the letters/numbers panels»
The diacritics panel is no longer a small tile grid: it is a **full keyboard with the numeric panel's exact anatomy — four rows of four wide keys**, rendered through **the very same themed key element, row height and margins** the letters/numbers panels use, with identical press feedback:

- **Row 1:** fathatan ً, dammatan ٌ, kasratan ٍ, dagger alef ٰ
- **Row 2:** fatha َ, damma ُ, kasra ِ, sukun ْ
- **Row 3:** shadda ّ, tatweel ـ, **the real DELETE key (hold-to-repeat)**, **the real SPACE key**
- **Row 4:** the shadda combos َّ ُّ ِّ ّْ (two characters per tap)
- Combining marks render on the **dotted circle ◌** like real Arabic keyboards
- All the smart systems remain: **smart stacking** (a mark over a mark replaces it), the **most-used** strip above the keys, and the **remove-diacritics** tool in the header

### 2) The FIXED tasks bar — «shows 10 tasks only, changeable and editable»
- The bar above the suggestions strip is now **fixed: always exactly ten slots, no scrolling** — your pins lead in pin order, visible defaults and the catalogue fill the rest up to ten
- **Change them:** **LONG-PRESS any slot** to open the new slot editor over the keyboard area — pick any catalogue task to occupy that exact slot; the swap is materialized into the pinned list and survives restarts
- **Edit them:** the tools drawer (side-pull handle) stays the place to reorder (up/down), pin and unpin
- The advanced technical keys (advanced/dual levels) still ride the tail — they are keys, not tasks; the ten slots stay ten
- The hybrid display-level cycle moved into the drawer

### 3) «Reorganize and develop all panels with an organized smart system»
- **Smart ordering:** the three smart panels' switcher chips **reorder themselves from LOCAL panel-open counters** — the current panel leads, the rest follow usage, ties break by catalogue order — with a new settings switch (Smart panels → Smart panel ordering)
- Every panel records its opens **locally only** (no text, no timestamps) and **incognito records nothing at all**
- The smart symbols and extended letters panels share the same anatomy, header and smart switcher

**Tests:** 394 unit tests passing (was 373) — 21 new tests covering the fixed ten slots, slot replacement, the smart ordering and the harakat keyboard arrangement.

**Strings:** 7 new AR/EN keys (parity 2317 per language).

---

## التحميل / Download

- **APK:** `DRS-Smart-Keyboard-v1.16.0.apk`
- **SHA-256:** يُنشر في `SHA256SUMS.txt` مع الأصول بعد اكتمال CI
- التثبيت موضعي آمن فوق الإصدارات السابقة (نفس شهادة التوقيع)

## What's Changed (commits)
- الجولة الشاملة السادسة عشرة — V 1.16.0: لوحة الحركات كلوحة مفاتيح كاملة، شريط المهام الثابت بعشر خانات مع محرر الخانة، والترتيب الذكي للوحات

**Full Changelog**: https://github.com/CTO-DRS/keyboard-drs-smart/compare/v1.15.0...v1.16.0
