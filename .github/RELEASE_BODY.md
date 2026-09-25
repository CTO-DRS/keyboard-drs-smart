<div align="center">

# DRS Smart Keyboard V 1.11.0

## الجولة الشاملة الحادية عشرة — نافذة التعديل المنبثقة فوق الشاشة: خارج اللوحة، تعتيم، التطبيق الخلفي مرئي، ولوحة النظام تعمل داخلها

**Eleventh Comprehensive Round — The Popup Editor Window Above the Screen: Outside the Panel, with a Scrim, the App Visible Behind, and the System Keyboard Working Inside**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.11.0 — نافذة التعديل المنبثقة فوق الشاشة

- 🪟 **«تعديل النص» يفتح نافذة عائمة حقيقية خارج اللوحة**: كما طلب
  المستخدم تمامًا — لم يعد المحرر محشورًا داخل لوحة الحافظة؛ اختيار
  **«تعديل النص»** من قائمة العنصر المطوّلة يفتح الآن **نافذة منبثقة
  بأسلوب حوارات النظام تطفو فوق الشاشة كلها**: التطبيق الذي كنت فيه
  يبقى **مرئيًا خلف ستارة تعتيم**، والنافذة عائمة في المنتصف — اضغط
  خارجها وتُغلق بأمان
- 🖥️ **المحرر الذكي الكامل داخل النافذة**: كل قدرات المحرر من الجولة
  العاشرة تعمل الآن داخل النافذة العائمة — **بحث واستبدال** حرفي
  بتنقل ملفوف وعدّاد مطابقات، **ستة عشر خوارزمية ذكية** قابلة
  للتراجع، **خمس عائلات خطوط × أربع درجات حجم**، **مشاركة** عبر
  ورقة النظام، **حفظ كملف** باسم تختاره، **تراجع/إعادة** بخمسين
  حالة، سطر الإحصاءات الحية، وعدّاد سقف 50,000 حرف
- ⌨️ **لوحة مفاتيح النظام تعمل داخل النافذة**: النافذة تُعيد ضبط
  حجمها مع ظهور الكيبورد (adjustResize) فتحرّر النص بحرية كاملة
  حتى داخل النافذة المنبثقة نفسها
- 🎯 **تسليم بلا حدود أحرف**: النص ينتقل من الكيبورد إلى النافذة عبر
  **مخزن تسليم بعملية واحدة بالضبط** (exactly-once) داخل العملية نفسها —
  لا Intent إطلاقًا، فلا حدود حجم حتى عند سقف 50,000 حرف، والعنصر
  الأصلي يُحفظ بهويته وتثبيته
- 🧭 **سياسة توجيه نقية وقرار رجوع صادق**: النصوص الحقيقية تذهب إلى
  النافذة المنبثقة، والوسائط وحالة النص الفارغة دفاعيًا تبقى مع
  المحرر داخل اللوحة — وإن منع النظام فتح النافذة (وهو نادر) فالطلب
  **يرجع تلقائيًا إلى المحرر داخل اللوحة** بدل أن يضيع: قرار النقطة
  الفاشلة مكتوب كدالة نقية مثبتة بالاختبارات
- ⚙️ **مسار حفظ محركي واحد**: الحفظ من النافذة يمر بنفس
  `editClipText` المحركي: رفع الطابع الزمني فيطفو العنصر للمقدمة،
  بقاء التثبيت والهوية، ومزامنة المقصوصة الأساسية إن كانت هي العنصر —
  سلوك مطابق للمحرر داخل اللوحة حرفيًا، مع توست تأكيد «تم حفظ
  التعديلات»
- 🌐 **مفتاحا سلاسل AR/EN جديدان** (توست الحفظ وتلميح النافذة
  المستقلة)، بفحص توازٍ آلي (PARITY OK — **2171** مفتاحًا لكل لغة).
- ✅ **266 اختبار وحدة ناجحًا** (كانت 256): 10 اختبارات جديدة تغطي
  سياسة التوجيه (النص ← النافذة، الوسائط والنص الفارغ الدفاعي ←
  اللوحة)، عقد الرجوع الصادق باتجاهاته الثلاثة، مخزن التسليم كاملًا
  (الفراغ، القراءة بلا استهلاك، الاستهلاك مرة واحدة بهوية العنصر
  ووضوحه، الاستبدال، والنص الفارغ الدفاعي)، وعقد هندسة النافذة
  (الكسور داخل (0,1] وحساب البكسلات بحارس الشاشة الصفرية).

## لماذا هذا مهم

التحرير لم يعد حبيس اللوحة: **نافذة حقيقية تطفو فوق تطبيقك** تعطيك
مساحة كاملة وإحساس نظام أصيل — عدّل نصًا منسوخًا والرسالة أمامك خلف
التعتيم، انسخ منها إلى أي مكان، شاركها، واحفظها ملفًا — ثم اضغط خارج
النافذة فتعود إلى سياقك بلا أي أثر. وكل ذلك **دون إنترنت، ودون مغادرة
جهازك**، وسياسة التوجيه والرجوع نقية مختبرة تعمل محليًا على الجهاز.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.11.0.apk` — ثبّته مباشرة (ترقية
  موضعية آمنة فوق أي إصدار سابق، نفس مفتاح التوقيع)
- **AAB**: للاحتياجات المتقدمة
- **SHA256SUMS.txt**: تحقق تشفيري كامل من كل الأصول
- **حزم السمات** (8): سمات إضافية اختيارية تُثبَّت لاحقًا

## الخصوصية

كل شيء يعمل **دون اتصال بالإنترنت إطلاقًا** على مسار الكتابة. النافذة
المنبثقة نشاط داخلي في التطبيق نفسه بلا أي اتصال، والنص ينتقل داخل
العملة المحلية عبر مخزن التسليم ولا يكتب في أي مكان إلا حين تختار
الحفظ، والمشاركة تمر عبر ورقة النظام إلى التطبيق الذي تختاره أنت —
والإحصاءات تبقى عدّادات مجهولة على جهازك — كما في كل الجولات السابقة.

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

## What's new in V 1.11.0 — the popup editor window above the screen

- 🪟 **«Edit text» now opens a real floating popup window outside the
  panel**: exactly as requested — the editor no longer lives inside
  the clipboard panel; choosing **«Edit text»** from an item's
  long-press menu opens a **system-dialog-styled popup window floating
  above the whole screen**: the app you were in stays **visible behind
  a dimmed scrim**, and the window floats centered — tap outside it
  and it closes safely.
- 🖥️ **The full smart editor inside the window**: every capability
  from the tenth round now runs inside the floating window — literal
  **find & replace** with wrap-around navigation and a match counter,
  **sixteen smart algorithms** (all undoable), **five font families ×
  four size steps**, **sharing** via the system sheet, **save-as-file**
  with a chosen name, a fifty-state **undo/redo**, the live stats row,
  and the 50,000-character cap counter.
- ⌨️ **The system keyboard works inside the window**: the window
  resizes with the keyboard (adjustResize) so you can type freely even
  inside the popup itself.
- 🎯 **Handoff with no size limits**: the text travels from the IME to
  the window through an **exactly-once in-process store** — no Intent
  at all, so no size limit even at the 50,000-character cap, and the
  original item keeps its identity and pin state.
- 🧭 **A pure routing policy with an honest fallback**: real text items
  go to the popup window; media and the defensive null-text case stay
  with the in-panel editor — and if the system blocks the window launch
  (rare), the request **automatically falls back to the in-panel
  editor** instead of being lost: the edge-case decision is written as
  a pure, test-pinned function.
- ⚙️ **One engine save path**: saving from the window walks the same
  `editClipText` engine path: bumped timestamp floats the item, pin and
  id preserved, primary clip re-synced when it is the same item —
  behavior identical to the in-panel editor, with an «Edits saved»
  confirmation toast.
- 🌐 **2 new AR/EN string keys** (the save toast and the independent-
  window hint), parity-checked automatically (PARITY OK — **2171**
  keys per language).
- ✅ **266 unit tests passing** (was 256): 10 new tests covering the
  routing policy (text → window; media and defensive null-text →
  panel), the honest fallback contract in all three directions, the
  full handoff store (empty state, peek without consuming, exactly-once
  consume with item identity, overwrite, the defensive empty-string
  case), and the window geometry contract (fractions inside (0,1] and
  px math with the zero-screen guard).

## Why it matters

Editing is no longer trapped in the panel: **a real window floats above
your app** with full space and a native system feel — edit a copied text
while your message waits behind the scrim, copy from it anywhere, share
it, save it as a file — then tap outside the window and return to your
context with no trace. And it is all **fully offline, fully on-device**,
with the routing and fallback policies pure, tested functions running
locally.

## Download

- **APK**: `DRS-Smart-Keyboard-v1.11.0.apk` — install directly (safe
  in-place upgrade over any previous release, same signing key)
- **AAB**: for advanced needs
- **SHA256SUMS.txt**: full cryptographic verification of every asset
- **Theme packs** (8): optional add-on themes, installable later

## Privacy

Everything runs **fully offline**: no internet on the typing path at
all. The popup window is an in-app activity with no network at all; the
text travels inside the local process through the handoff store and is
written nowhere unless you choose to save; sharing goes through the
system sheet to the app **you** choose; and statistics remain anonymous
counters on your device — same as every round before this one.

</div>
