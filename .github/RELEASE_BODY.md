<div align="center">

# DRS Smart Keyboard V 1.12.0

## الجولة الشاملة الثانية عشرة — نظام الحافظة الذكي الكامل: بطاقات نتائج ملونة مع تنقل، كشف الأسطر البرمجية، 500,000 حرف، إعدادات في التطبيق، وتنظيم شامل للوحة

**Twelfth Comprehensive Round — The Complete Smart Clipboard System: Colored Result Cards with Navigation, Code-Line Detection, 500,000 Characters, In-App Settings, and a Fully Organized Panel**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.12.0 — نظام الحافظة الذكي الكامل

- 🎨 **بطاقات نتائج البحث الملونة مع التنقل**: في المحرر الذكي (النافذة
  المنبثقة والمحرر داخل اللوحة معًا)، كل مطابقة بحث صارت **بطاقة ملونة**
  تعرض رقم السطر والسياق من نفس السطر مع إبراز المطابقة نفسها بلون
  البطاقة — والبطاقات تتنقل بين ستة ألوان مميزة ثابتة. **اضغط أي بطاقة
  فتقفز المطابقة** إلى موضعها: عدّاد «{active} من {count}» يتحدث، والتطابق
  النشط يتوهج أقوى من باقي المطابقات. ولوحة النتائج تفتح تلقائيًا مع بدء
  البحث (وقابلة للطي والفتح برقاقة واحدة).
- ✨ **المطابقات تتوهج داخل النص نفسه**: لم تكتفِ البطاقات — كل مطابقة
  في حقل التحرير تُلوَّن بلون بطاقتها، والمطابقة النشطة أقوى ظلًا، فالبطاقات
  والنص يحكيان قصة واحدة.
- 🖥️ **بحث اللوحة نفسه صار بطاقات ملونة**: عند البحث في سجل الحافظة تظهر
  النتائج **بطاقات ملونة قابلة للتنقل** — كل بطاقة تعاين أول إصابة مع
  إبرازها وشارة فئتها، الضغط عليها يفتح سلّم إجراءات العنصر الكامل
  (والضغط المطول يلصق فورًا)، مع عدّاد «N نتيجة» في الأعلى.
- 💻 **كاشف الأسطر البرمجية**: كاشف نقي خالص يفحص النص سطرًا سطرًا
  (أقواس، فواصل منقوطة، تعليقات، معاملات إسناد، مفردات لغوية، أشكال
  وسوم وJSON وCSS) ويقرر: هل هذا كود؟ وبأي لغة؟ من **تسع عائلات**:
  Kotlin/Java، Python، JavaScript، JSON، HTML/XML، CSS، SQL، C/C++،
  وBash — مع **شارة حية** في المحرر «💻 {اللغة} — {code} من {total}
  سطرًا برمجيًا» و**تبديل تلقائي إلى الخط أحادي المسافة** عند فتح
  مقطع كود، وثلاث رقاقات جديدة: **ترقيم الأسطر** (1. 2. 3.)،
  **استخراج الكود** (يُبقي الأسطر البرمجية فقط)، و**حذف الأسطر
  البرمجية** — كلها قابلة للتراجع.
- 📏 **سقف 500,000 حرف**: رفعنا سقف السياسة من 50,000 إلى **500,000 حرف**
  للنص الواحد كما طلب المستخدم — القص عند الحد يبقى آمنًا مع أزواج
  الإيموجي، والمقصوصة الأساسية تحتفظ بالنص الكامل. ومع السقف الجديد:
  **تحذير «نص ضخم»** عند تجاوز 100,000 حرف تحسبًا للتباطؤ.
- ⚙️ **إعدادات الحافظة في التطبيق**: مجموعة جديدة كاملة في شاشة
  الإعدادات «المحرر الذكي والنتائج» بثمانية إعدادات: **سقف أحرف
  المحرر** (50K/100K/250K/500K)، **بطاقات نتائج البحث الملونة**
  (تشغيل/إيقاف)، **فتح لوحة النتائج تلقائيًا**، **كشف الأسطر
  البرمجية**، **خط المحرر الافتراضي** (الخمس عائلات)، **حجم الخط
  الافتراضي** (الدرجات الأربع)، **مطابقة حالة الأحرف افتراضيًا**،
  و**ترتيب سجل الحافظة** — والمحررات كلها تحترم هذه الافتراضيات
  من أول فتح.
- 🗂️ **تنظيم شامل للوحة الحافظة**: استبدلنا تقسيم «مثبت/حديث/أقدم»
  بأقسام **تقويمية حية**: **مثبت ← اليوم ← أمس ← هذا الأسبوع ← هذا
  الشهر ← أقدم** (بحارس حدّ السبعة والثلاثين يومًا المختبر). وأضفنا
  **أربعة أوامر فرز محفوظة** ضمن صف التصفية: الأحدث، الأقدم، الأطول،
  الأقصر — بلا تكرار ولا خلط، وكسر التعادل بالطابع الزمني.
- 🏷️ **شارات فئات ذكية**: كل بلاطة نصية تُفحص لحظيًا وتُوسم بشارة
  الفئة: **🔗 رابط** أو **✉️ بريد** أو **📞 رقم هاتف** أو **💻 كود** —
  والنص العادي يبقى بلا شارة تفاديًا للضوضاء. البطاقات الناتجة عن
  البحث تحمل الشارة نفسها.
- 🌐 **57 مفتاح سلاسل AR/EN جديدًا**، بفحص توازٍ آلي (PARITY OK —
  **2228** مفتاحًا لكل لغة).
- ✅ **308 اختبار وحدة ناجحًا** (كانت 266): 42 اختبارًا جديدًا تغطي
  بطاقات النتائج (أرقام الأسطر، حدود السياق وعدم عبور السطر، رايات
  القص الصادقة، دوران الألوان وسلامة السالب، سقف البطاقات)، نطاقات
  الإبراز داخل النص، كاشف الكود (مقاطع Kotlin وPython وJavaScript
  وJSON وHTML، نفي النثر العربي، الحد الأدنى للأسطر، حدود الثقة،
  استخراج وحذف الأسطر البرمجية)، السياسة الجديدة 500K والحدود
  الفعّالة للمحرر وسلامة الإيموجي عند القص، ترقيم الأسطر، الفرز
  الأربعة بلا طفرات، الأقسام التقويمية بحوافها الدقيقة، الفئات
  الذكية بأمثلتها العربية واللاتينية، وبطاقات بحث اللوحة بالسقوف
  والألوان وتخطي الوسائط.

## ماذا يعني هذا عمليًا؟

انسخ كودًا من أي تطبيق → افتح «تعديل النص» → تظهر لك شارة اللغة وتتحول
الخطوط تلقائيًا لأحادي المسافة. ابحث عن أي كلمة داخل نص طويل → بطاقات
ملونة مرقمة بالأسطر، اضغط واحدة فتتوهج مطابقتها داخل النص. والوحة
الحافظة نفسها صارت يومًا مقروءًا: أقسام تقويمية، فرز من اختيارك،
وشارات ذكية — وكل شيء قابل للتخصيص من إعدادات التطبيق.

<div dir="ltr">

## What's new in V 1.12.0 — The Complete Smart Clipboard System

- 🎨 **Colored search-result cards with navigation** in both editors:
  every match becomes a colored card (line number + same-line context
  with the match highlighted) cycling a six-color palette; tapping a
  card jumps the match counter and glows the active match inside the
  text itself. The panel's own search now renders navigable colored
  result cards too.
- 💻 **Programming-line detection**: a pure, line-by-line heuristic
  (braces, semicolons, comments, assignment operators, keyword
  vocabulary, tag/JSON/CSS shapes) recognizes nine language families
  (Kotlin/Java, Python, JavaScript, JSON, HTML/XML, CSS, SQL, C/C++,
  Bash), shows a live «{lang} — {code} of {total} code lines» badge,
  auto-switches to monospace at open, and adds three undoable chips:
  number lines, extract code, remove code lines.
- 📏 **500,000-character cap** (up from 50,000), surrogate-safe at the
  boundary, with a user-chosen editor limit (50K/100K/250K/500K) and a
  huge-text slowdown warning past 100,000.
- ⚙️ **Eight new in-app settings** under «Smart editor & results»:
  editor character limit, colored result cards, auto-open results
  panel, code detection, default editor font & size, match-case by
  default, and the history sort order.
- 🗂️ **Comprehensive panel organization**: calendar sections (Pinned /
  Today / Yesterday / This week / This month / Older), four persisted
  sort orders (newest, oldest, longest, shortest), and smart category
  badges on text tiles (link / email / phone / code).
- 🌐 **57 new AR/EN string keys** (PARITY OK — 2228 per language).
- ✅ **308 unit tests passing** (was 266) — 42 new pure-contract tests.

</div>

## التحميل

| الأصل | الوصف |
|---|---|
| `DRS-Smart-Keyboard-v1.12.0.apk` | الحزمة الكاملة الموقّعة (versionCode 24) |
| `DRS-Smart-Keyboard-v1.12.0.aab` | حزمة أندرويد للمتاجر |
| `SHA256SUMS.txt` | بصمات SHA-256 لكل الأصول |

</div>
