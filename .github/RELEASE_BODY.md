<div align="center">

# DRS Smart Keyboard V 1.27.0

## الجولة الشاملة السابعة والعشرون — الأذن الثانية: الاستماع يعلن نفسه، والمحرر يسمع

**Twenty-Seventh Comprehensive Round — The Second Ear: the Listening State Declares Itself and the Editor Hears**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.27.0 — جولة الأذن الثانية

فحص ثلاثي جديد تحقق من مرشحات الجولة الماضية بالكود الفعلي: شريط الاستماع
كان يرسم نفسه بقواعد «الشريط الذكي» في **كل** ثيم بلا أي قدرة على تمييزه،
ومحرر الحافظة المنبثق — نافذة حقيقية فوق الشاشة كلها — كان **أصم** من
الكلام رغم أن بنية النشاط تسمح باستضافة حوار الإذن مباشرة، وصفحة عرض
الإضافة كانت تستقبل مستخدمها بمعرّف ترخيص خام مثل `Apache-2.0` بدل اسمه
الذي يعرفه الناس. وTODOان غامضان في مقابض النافذة وسجل الأقراص كانا يعدان
بتقييم لا يُقرأ في أي مكان. كل بند أدناه عمل حقيقي مُختبر، لا ترقيم أسطر.

- 🎨 **حالة الاستماع تُلتحق بسطح الثيم — دون كسر ثيم واحد قائم** —
  شريط الاستماع يبقى يرسم نفسه بقواعد `smartbar` **بالضبط كما هي**: قاعدة
  بلا سمات تطابق أي استعلام مهما حمل من سمات، فكل الثيمات المدمجة
  والمخصصة تُظهر الشريط بالوجه نفسه حرفيًا. لكن العنصر الجذري يحمل الآن
  سمة `voice`، فيستطيع أي ثيم إضافة قاعدة ``smartbar[voice=`true`]`` تلوّن
  شريط الاستماع وحده — **وراثة بالبناء لا تكرار قواعد**. ومحرر الثيمات
  نال رقاقة «حالة الاستماع» الجديدة، وسمة نمط نافذة الإدخال التي كانت
  معروضة بنص إنجليزي مصلّب في واجهة عربية صارت عربية أخيرًا.

- 🎙️ **محرر الحافظة يسمع — إملاء داخل النافذة المنبثقة** — زر ميكروفون
  جديد في المحرر يدير متعرّف المنصة داخل نافذته: النص الجزئي يظهر حيًا
  في شريط رقيق لا يشغل أي مساحة في الخمول، والنص النهائي يُلحق بمحتوى
  المحرر مباشرة — لا `InputConnection` هنا أصلًا، فالحقل حالة Compose
  خالصة. **الإلحاق عقد نقي مختبر** (`ClipDictationPlan.join`): كلام
  فارغ لا يصنع تعديلًا مُفبركًا، فاصل واحد فقط، والفراغ الختامي الذي
  كتبه المستخدم يُحفظ ولا يُضاعف، وسقف التخزين يقصّ **تمامًا كما يقصّ
  الكتابة اليدوية**، والنص الذي ابتلعه السقف كله ليس تعديلًا. والنشاط
  نافذة حقيقية فيستضيف حوار إذن الميكروفون مباشرة، ورفضه يمر بالبوابة
  الصادقة نفسها التي بُنيت لإملاء لوحة المفاتيح في v1.26: رفض عادي =
  توست الشرح، رفض نهائي = صفحة إعدادات التطبيق. والمتحكم يسكن بالكامل
  داخل نافذة المحرر — لا تماس مع حافلة إملاء لوحة المفاتيح، فلا جلسة
  ميكروفون تتجاوز نافذتها.

- 📜 **الترخيص بلغة الناس — أقدم TODO في شاشة الإضافات يموت** —
  الدالة النقية `extensionLicenseDisplayName` تحوّل معرّفات SPDX
  المعروفة إلى أسمائها الرسمية (`Apache-2.0` ← «Apache License 2.0»،
  مطابقة غير حساسة لحالة الأحرف لأن المانيفستات يكتبها بشر)، وتفهم
  تعبيرات `OR`/`AND`/`WITH` عاملًا عاملًا وتحفظ الأقواس، **والمعرّف
  المجهول يمرّ كما هو** — معرّف SPDX خام هو الجواب الصادق حين لا نعرف
  الاسم الرسمي، لا اسم مُلفّق.

- 🧭 **TODOان يُحسمان بقرارات موثقة لا بوعود معلقة** — مقابض تحريك
  وتحجيم نافذة الإدخال: مخطط تراكم الإزاحة (spec ابتدائي مُجمّد +
  تحويل dp لكل حدث) هو الصحيح كما بُني — البديل بالإحداثيات المطلقة
  يمر بنفس تحويلات dp فلا يمكن أن يكون أدق — والخلاصة موثقة في الكود
  بدل سؤال معلق. و`Flog.fileLog` الفارغة: مسار ميت إنتاجيًا (لا أحد
  يطلب `OUTPUT_FILE` أصلًا، وفرعا الـwhen لا يعملان معًا)، وتسجيل
  ملفات حقيقي قرار تصميم كامل (الموقع، التدوير، الخيط الكاتب، سياسة
  الاحتفاظ) لا يُصنع داخل دالة مساعدة — الخلاصة موثقة والسجل يبقى
  logcat حتى يوجد مستهلك حقيقي.

- 🌐 **6 مفاتيح سلاسل AR/EN جديدة** عبر `add_v1270_strings.py` بالنمط
  المرجعي نفسه: idempotent ببوابة فرق المجموعات التي تفشل بصوت عالٍ
  عند أي انفراد — توازي 2,423 مفتاحًا لكل لغة.

</div>

<div dir="ltr">

## New in V 1.27.0 — The Second Ear

- 🎨 **The listening state joins the theme surface — without breaking a
  single existing theme** — the dictation bar still paints itself
  through the exact `smartbar` rules every theme already owns (an
  attribute-less rule matches any attribute query). But the root
  element now carries the `voice` attribute, so any theme can add
  ``smartbar[voice=`true`]`` rules to paint the listening bar alone —
  **inheritance by construction, not rule duplication**. The theme
  editor gains the new listening-state chip, and the ime-window-mode
  attribute finally speaks the UI language instead of a hardcoded
  English string.

- 🎙️ **The popup clipboard editor hears** — a new dictate button drives
  the platform recognizer inside the editor window: the live partial
  transcript shows in a thin strip that occupies no space while idle,
  and the final transcript is appended straight into the editor's text
  state — no InputConnection exists here, the field is plain Compose
  state. **The append is a tested pure contract**
  (`ClipDictationPlan.join`): blank speech never fabricates an edit, a
  single space joins, the user's own trailing whitespace is preserved
  instead of doubled, the storage limit truncates exactly like the
  typed path, and a transcript swallowed whole by the limit is an
  honest no-op. The activity hosts the mic permission dialog directly,
  and a denial walks the same honest gate built for the IME route in
  v1.26: plain denial = explanation toast, permanent denial = the app's
  settings page. The controller lives and dies entirely inside the
  editor window — no session ever crosses into the keyboard's dictation
  bus.

- 📜 **Licenses speak the world's language** — the pure
  `extensionLicenseDisplayName` maps known SPDX ids to their official
  titles (`Apache-2.0` → "Apache License 2.0", case-insensitive because
  manifests are written by humans), honors `OR`/`AND`/`WITH` expressions
  operand by operand, preserves parentheses, and passes unknown ids
  through untouched — a raw SPDX id is the honest answer when no
  official title is known, never a made-up one.

- 🧭 **Two ambiguous TODOs close as documented decisions** — the
  ime-window editor handles' drag accumulation is correct as built
  (frozen initialSpec + per-event dp conversion; the absolute-coordinates
  alternative passes through the same conversions and cannot be more
  accurate), and the empty `Flog.fileLog` stays a deferred design
  decision with the dead OUTPUT_FILE path and the when-branch semantics
  spelled out — logcat remains the debug surface until a real consumer
  exists.

- 🌍 **6 new AR/EN string keys** via the reference-pattern
  `add_v1270_strings.py` (idempotent, with the hard parity gate) —
  parity is now **2,423 keys per language**.

- 🧪 **15 new unit tests (613 passing, was 598)** covering every pure
  contract above, plus the attribute-matching engine semantics pinned
  in lib/snygg's own suite.

## Install

1. Download `DRS-Smart-Keyboard-v1.27.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Voice dictation: tap the microphone (in the keyboard or in the popup
clipboard editor); on the first press Android will ask for the
microphone permission — grant it and speak. If it was permanently
denied before, the denied press opens the app's system settings so you
can re-enable it. While listening, the bar shows the live transcript,
your actual voice wave, and the «On device» chip whenever the session
runs on the local recognizer.

Theme designers: add ``smartbar[voice=`true`]`` rules to style the
listening bar on its own — every other smartbar rule keeps applying
underneath, so a two-line override is all it takes.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Voice dictation is the one
explicit exception you trigger per press and fully control: it delegates
to your platform's recognition service only when you tap the mic — in the
keyboard or in the popup editor — and the bar shows you, session by
session, when your speech stays on the device. Read the full `PRIVACY.md`.

</div>
