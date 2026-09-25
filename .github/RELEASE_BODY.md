<div align="center">

# DRS Smart Keyboard V 1.21.0

## الجولة الشاملة الحادية والعشرون — السلامة والدقة والوصول: مفتاح ميت يُحيى، نافذة انهيار تُسدّ، ووعد v1.11 يتحقق أخيرًا — نافذة التعديل الإشعارية

**Twenty-First Comprehensive Round — Safety, Precision & Accessibility: A Dead Key Revived, A Crash Window Sealed, and the v1.11 Promise Finally Shipped — the Notification-Style Edit Window**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.21.0 — جولة السلامة والدقة والوصول

جولة فحص ثلاثي جديدة (محرك الإدخال واللوحات / الحافظة والإيموجي واللوحات
الذكية / طبقة التطبيق والإعدادات والنشر) كشفت أخطاءً حقيقية كان الكود
يكذبنا بوجودها: مفتاح يعلن الحذف ولا يحذف، انهيار يمكن للمستخدم إحداثه
بنقرتين، وقسيمة وعدٍ من v1.11 لم يصرفها أي إصدار. كل بند أدناه إصلاح
مُختبر فعليًا، لا لمسة تجميل.

- 🛡️ **وعد v1.11 يتحقق أخيرًا: نافذة الاشعارات المنبثقة الخاصة بالتعديل** —
  منذ v1.11 والمستخدم يطلب «نافذة الاشعارات المنبثقه الخاصه بالتعديل وليست
  في اللوحة»، وما كان موجودًا فعلًا سوى نافذة عائمة تُفتح من داخل لوحة
  الحافظة — ولا أي إشعار في المستودع بأكمله (صفر استخدامات
  `NotificationCompat`/`Notification` خارج مركز التحديث). الآن: عند التقاط
  نص جديد يظهر **إشعار فوري (heads-up)** بقناة عالية الأهمية، عنوانه
  «تعديل النص المنسوخ» ومعاينة النص، وفعل **«تعديل»** يفتح نافذة المحرر
  العائمة **فوق الشاشة — خارج لوحة المفاتيح** من أي مكان، واللوحة مغلقة
  أو لا. اللمسات الدقيقة: النص يعبر عبر Intent إضافي فينجو من موت العملية
  (التسليم الداخلي بالذاكرة كان سيفشل من إشعار لاحق)، النصوص **الحساسة**
  (كلمات السر المكتشفة) وغير النصية لا تظهر في الإشعارات أبدًا (خصوصية
  شاشة القفل)، إشعار واحد يُستبدل بجديدك التالي (لا تكدّس)، وفيل الإعداد
  في شاشة إعدادات الحافظة الشاملة: **«إشعار «تعديل» عند النسخ»**.

- 🔧 **مفتاح الحذف في لوحة الحركات كان ميتًا عند اللمسة السريعة** —
  مفتاح الحذف بضغطة سريعة أقصر من 400 ميلي ثانية **لم يحذف شيئًا**:
  النقر كان مستبعدًا صراحة عن مفتاح التكرار، والتأثير المتكرر كان ينام
  400ms قبل أول حذف — فمفتاح أساسي في لوحة حركات كاملة كان يعمل بالضغط
  المطول فقط. الآن أول حذف ينطلق لحظة ملامسة الإصبع ثم يتكرر عند
  الاستمرار — اللمسة السريعة تحذف، والضغط المطول يكنس.

- 💥 **نافذة انهيار حقيقية في المحرر الذكي — مغلقة** — البحث في المحرر
  يحسب المطابقات على لقطة نص مؤجلة بـ150ms، لكن النص نفسه ينكمش خلفها
  (استبدال بنص أقصر، تراجع عن إضافة كبيرة): إعادة التركيب كانت تمرر
  مطابقات قديمة إلى `text.substring` فتنهار `StringIndexOutOfBounds`
  فوق بطاقات النتائج أو التوهج داخل النص — بنقرات عادية في المحررين
  معًا (النافذة العائمة ومحرر اللوحة). الحارس: مُطبِّع نقي يُسقط
  المطابقات الخارجة كليًا ويقص الجزئية منها، تحته حلقة التوهج تُفلتر
  مجددًا ضد طول النص الحي لحظة التطبيق، **وكل بطاقة ناجية تحمل فهرسها
  الأصلي** في قائمة المطابقات فيبقى التنقل (الأسهم والبطاقة النشطة)
  صاديًا على الهدف الحقيقي.

- 🚫 **الإنذار الكاذب في كشف الأسطر البرمجية** — سطر يبدأ بـ«*» كان
  يُحكم «كودًا» فورًا (ظنًّا أنه استمرار توثيق JSDoc) — فالقائمة
  النقطية الماركداونية بثلاثة أسطر («* عنصر واحد…») كانت تُقلب المحرر
  كله إلى خط أحادي المسافة بشارة «كود» زائفة. العلامة الغامضة لم تعد
  تقرر وحدها: تُجرد والسطر الباقي يُقيَّم بقواعده، فالقوائم قوائم
  (لا كود)، و«* let x = 5;» ما تزال كودًا، و`/**` و`*/` و`//` وshebang
  تقرر فورًا كما كانت.

- 🕌 **طمس التشكيل صار واحدًا** — محرر الحافظة كان يُبقي علامات ضبط
  المصحف الصغيرة (U+06D6–U+06ED) بينما لوحة أدوات النص تنزعها — نفس
  الأداة باسمين وسلوكين. الاثنان يشاركان تعريفًا واحدًا الآن: «طمس
  التشكيل» يعني الشيء نفسه في كل مكان.

- 🌗 **حارس نافذة «تتبع الوقت» المعكوسة** — من يضبط شروقًا 22:00 وغروبًا
  06:00 (جدول ليلي) كان يجبر الثيم الليلي **إلى الأبد** لأن المدى
  `current in sunrise..sunset` ينهار إلى فراغ. نافذة النهار تلتف حول
  منتصف الليل الآن، والزوج المنحل (شروق = غروب) يعود إلى النهار.

- ✍️ **16 حرفًا عربيًا نالت حركاتها** — التاء والثاء والحاء والخاء والدال
  والذال والراء والسين والصاد والضاد والطاء والظاء والعين والغين والميم
  والنون كانت **بلا أي ضغطة مطولة** في خريطة النوافذ العربية — أبرز أحرف
  الكتابة اليومية بلا طريق إلى حركاتها! كل واحدة تحمل الآن الحركات
  الخمس (َ ُ ِ ّ ْ) بضغطة مطولة. والفاصلة العربية «،» التي كانت غائبة
  كليًا من لوحة الرموز الشرقية (بينما «؛» موجودة!) أصبحت الضغطة المطولة
  على «؛» مع التطويل «ـ» بجوارها.

- 🧭 **أربع أدوات إدراج علامات الاتجاه والوصل** — قسم جديد في لوحة أدوات
  النص: **RLM** و**LRM** (تثبيت اتجاه النيُترالات بين النصوص المختلطة)،
  **ZWJ** (واصل الحروف والإيموجي المركبة)، و**ZWNJ** — نصف المسافة —
  (فاصل الاتصال بلا مسافة). الإدراج عند المؤشر مباشرة، يعمل على حقل فارغ،
  ولا يبديل أي حرف — كانت أدوات الإزالة موجودة منذ v1.5 والإدراج غير
  موجود في المستودع كله.

- 👋 **منتقي لون البشرة داخل لوحة الإيموجي** — تغيير لون البشرة كان
  يتطلب الخروج من الكيبورد إلى إعدادات التطبيق (الإعداد موجود منذ بداية
  المشروع لكن بلا أي وصول من اللوحة!). نقطة ملونة في الصف السفلي تدور
  الستة ألوان (DEFAULT → LIGHT → … → DARK → DEFAULT) بألوان عيّنة twemoji
  الحقيقية، ومع تسمية وصولية عربية.

- 🗣️ **TalkBack يتكلم حيث كان صامتًا** — تبويبات الإيموجي العشر كانت
  أيقونات معلنة `Tab` بلا نص: «بلا تسمية». كل تبويب ينطق اسم فئته
  العربية الآن، وزرا «مسح البحث» في شاشتي الإضافات (أزرار فعلية بلا
  وصف) يُعلنان وظيفتهما، وزر لون البشرة ينطق «اختيار لون البشرة».

- 🎨 **صف بحث الإيموجي يلتزم ثيم لوحة المفاتيح** — كان يلوّن نفسه من
  `MaterialTheme` الافتراضي بينما كل لوحات أخرى تقرأ ثيم snygg الفعّال —
  نفس عائلة الخلل الذي أُصلح لبحث الحافظة في v1.7 ولم يصل إلى الإيموجي.
  يقرأ ألوان النافذة المُهيمنة الآن مع تدرج قراءة من الإضاءة.

- 💾 **قاموس المستخدم يسافر في النسخة الاحتياطية أخيرًا** — الكلمات
  المتعلمة (قلب التخصيص!) كانت **الشيء الوحيد المهم خارج الأرشيف**:
  إعادة ضبط المصنع كانت تمحوها ولا شيء يعيدها. خيار جديد «قاموس المستخدم
  (الكلمات المتعلمة)» في شاشة النسخ يصدّرها بصيغة القائمة المدمجة
  المعيارية (نفسها التي يقرأها الاستيراد اليدوي)، والاستعادة تدمجها بذكاء
  (تحديث الموجود، إدخال الجديد، دمج أو مسح-ثم-استيراد حسب استراتيجيتك)
  مع تجاهل صادق للأسطر التالفة.

- 🏗️ **خطوة lint في مسار النشر** — خط أنابيب الإصدار كان يشغّل 521
  اختبارًا وبوابة الوسم ثم ينشر، دون أي فحص ثابت. `lintRelease` يجري
  الآن مع كل إصدار (غير مانع أثناء الضجيج الحالي، لكنه مُنتَج للمراجعة).

- 🧹 **نظافة** — `RELEASE_BODY.md` الجذري كان نسخة ميتة من v1.16 تعارض
  نسخة `.github/` التي تستخدمها CI فعلًا — حُذف. وتعليق «50,000 حرف»
  القديم فوق ثابت 500,000 صار صادقًا. **التوازي الآن 2,390 مفتاحًا
  لكل لغة (AR/EN)**.

- 🧪 **20 اختبار وحدة جديدًا (521 ناجحة)** — عقود حارس المطابقات
  (`normalizeAgainst`/`buildCards`/`inBounds` بإسقاطها وقصّها وصمد
  فهارسها الأصلية)، عقود كشف الكود (القوائم ليست كودًا، النجم المتبوع
  بكود يظل كودًا، العلامات الواضحة تقرر)، طمس ضبط المصحف، نافذة
  FOLLOW_TIME بحالاتها الأربع (المعتاد، المعكوس، المنحل، الحواف الشاملة)،
  دورة لون البشرة وألوان العيّن، عقد سلسلة قاموس المستخدم (ذهابًا
  وإيابًا، locale فارغة، تلف وفكرة)، بوابة الإشعار بحالاتها الست
  (مفعّل/حساس/غير نصي/فراغ/إعداد/سجل) ومعاينتها، خرائط علامات الإدراج
  ونطاق التوزيع الممتد (-657)، و**عقد بنية الأصول**: الـ16 حرفًا بحركاتها
  في ar.json والفاصلة العربية والتطويل في الرموز الشرقية — الأصول
  التي تخدم أصابعك محروسة كالكود.

</div>

<div dir="ltr">

## About the project

**DRS Smart Keyboard** is a free, open-source Android keyboard
(Apache-2.0) built with Kotlin, Jetpack Compose and Material 3 —
**Arabic-first**: a fully Arabic interface with native RTL support,
thoughtful Arabic layouts and Arabic suggestions/correction, with English
as a complete second option. Everything stays on your device: no accounts,
no tracking, no ads.

## What's new in V 1.21.0 — The Safety, Precision & Accessibility Round

A fresh tri-front audit (input engine & layouts / clipboard, emoji & smart
panels / app layer, settings & release) found defects the code kept
denying: a key that claims to delete but doesn't, a crash any user could
trigger in two taps, and a v1.11 promise no release ever cashed. Every
item below is a real, tested fix — no polish passes.

- 🛡️ **The v1.11 promise finally shipped: the notification-style edit
  window** — since v1.11 the user asked for «نافذة الاشعارات المنبثقه
  الخاصه بالتعديل — وليست في اللوحة», and what shipped back then was a
  floating window reachable only from inside the clipboard panel — the
  repo had **zero notifications** outside the update center. Now: when a
  new text clip is captured, a **heads-up notification** (HIGH-importance
  channel) appears — "Edit the copied text", a preview, and an **«تعديل»
  action that opens the floating editor OVER the screen, outside the
  keyboard**, from anywhere, panel open or not. The details: the text
  rides intent extras so the popup survives process death (the in-memory
  handoff would fail from a later notification), **sensitive** clips
  (detected passwords) and non-text items never surface (lock-screen
  privacy), one notification replaces the previous (no stacking), and a
  switch in the comprehensive clipboard settings: **"«Edit» notification
  on copy"**.

- 🔧 **The harakat keyboard's delete key was dead on quick taps** — a tap
  shorter than 400 ms deleted **nothing**: clicks were explicitly excluded
  from the repeat key while the repeat effect slept 400 ms before its
  first deletion — a primary key on a shipped panel worked only by holding.
  The first delete now fires the instant the finger lands, then repeats on
  hold — quick tap deletes, long-press sweeps.

- 💥 **A real crash window in the smart editor — sealed** — editor search
  computes matches on a text snapshot debounced by 150 ms, while the text
  itself shrinks underneath (replace-with-shorter, undo a big insert):
  recomposition handed the stale matches to `text.substring`, throwing
  `StringIndexOutOfBounds` over the result cards or the in-text glow —
  reachable with ordinary taps in BOTH editors (the popup and the panel
  one). The guard: a pure normalizer drops wholly-out matches and clamps
  partial ones, the highlight loop re-filters against the live field text
  length at apply time, and **every surviving card keeps its ORIGINAL
  match-list index** so navigation (arrows, active card) stays honest.

- 🚫 **The false positive in code-line detection** — a line starting with
  `*` was instantly labeled code (assumed a JSDoc continuation), so a
  three-line Markdown bullet list flipped the whole editor to monospace
  with a bogus "code" badge. The ambiguous marker no longer decides alone:
  it is stripped and the remainder scored by the normal rules — bullets
  are bullets, `* let x = 5;` is still code, and `/**`, `*/`, `//` and
  shebangs still decide immediately.

- 🕌 **One tashkeel strip to rule them both** — the clipboard editor kept
  the small Quranic annotation marks (U+06D6–U+06ED) while the text-tools
  panel stripped them — one tool, two names, two behaviors. Both now share
  a single definition: "remove diacritics" means the same thing everywhere.

- 🌗 **A guard for the inverted FOLLOW_TIME window** — setting sunrise
  22:00 and sunset 06:00 (a night shift) forced the night theme **forever**
  because `current in sunrise..sunset` collapses to an empty range. The
  day window now wraps midnight, and the degenerate pair (sunrise ==
  sunset) falls back to day.

- ✍️ **16 bare Arabic letters got their harakat** — ت ث ح خ د ذ ر س ص ض ط
  ظ ع غ م ن had **no long-press at all** in the Arabic popup mappings —
  the workhorse letters of daily writing had no path to their diacritics!
  Each now carries the five core harakat (َ ُ ِ ّ ْ) on long-press. And
  the Arabic comma «،» — entirely absent from the eastern symbols page
  while «؛» sat right there — is now the long-press on «؛», with the
  tatweel «ـ» beside it.

- 🧭 **Four directional/joining mark insertion tools** — a new section in
  the text-tools panel: **RLM** and **LRM** (pin neutral characters
  between mixed-direction runs), **ZWJ** (join letters and emoji into
  compound sequences), and **ZWNJ** — نصف المسافة — (break the cursive
  join with no visible space). They commit straight at the cursor, work
  on an empty field, and replace nothing — the removal tool existed since
  v1.5 while insertion was absent from the entire repo.

- 👋 **An in-palette skin-tone selector** — changing the skin tone used to
  require leaving the keyboard for the app settings (the preference has
  existed all along with zero access from the palette!). A colored dot in
  the bottom row cycles the six tones (DEFAULT → LIGHT → … → DARK →
  DEFAULT) with real twemoji swatches and an Arabic accessibility label.

- 🗣️ **TalkBack speaks where it was silent** — the ten emoji tabs were
  icon-only `Tab`s: announced as unlabeled. Each tab now announces its
  Arabic category name, the clear-search buttons in the two extension
  screens (actionable icons with no description) announce their job, and
  the skin-tone dot announces "choose skin tone".

- 🎨 **The emoji search row follows the keyboard theme** — it painted
  itself from the default `MaterialTheme` while every other panel reads
  the active snygg keyboard theme — the same defect family fixed for the
  clipboard search back in v1.7 that never reached the emoji palette. It
  reads the themed window colors now with a luminance-derived fallback.

- 💾 **The user dictionary finally rides the backup** — your learned
  words (the heart of personalization!) were the one important thing
  outside the archive: a factory reset erased them and nothing brought
  them back. A new "User dictionary (learned words)" selector in the
  backup screen exports them in the standard combined-list format (the
  same one the manual import path reads), and restore merges them
  intelligently (update existing, insert new, merge or erase-then-import
  by your chosen strategy) while honestly skipping malformed lines.

- 🏗️ **A lint step in the release path** — the pipeline ran 521 unit
  tests and the tag gate, then published without any static analysis.
  `lintRelease` now runs with every release (non-fatal while the existing
  baseline is noisy, but produced for review).

- 🧹 **Hygiene** — the stale root `RELEASE_BODY.md` (a frozen v1.16 copy
  diverging from the `.github/` one CI actually uses) is deleted, and the
  old "50,000 characters" comment above the 500,000 constant now tells
  the truth. **Exact parity: 2,390 keys per language (AR/EN)**.

- 🧪 **20 new unit tests (521 passing)** — the match-guard contracts
  (`normalizeAgainst`/`buildCards`/`inBounds` with drop, clamp and
  preserved original indexes), code-detection contracts (bullets are not
  code, a star followed by code still is, unambiguous markers decide),
  the Quranic-mark strip, the FOLLOW_TIME window in all four states
  (normal, inverted, degenerate, inclusive edges), the skin-tone cycle
  and swatches, the user-dictionary format round trip (word+freq+locale+
  shortcut, literal-null global entries, malformed and out-of-range
  honesty), the notification gate in all its states and the preview
  capping, the insertion-mark maps and the extended dispatch range
  (-657), and — new this round — **asset-structure contracts**: the 16
  letters with their harakat in ar.json and the Arabic comma + tatweel in
  the eastern symbols page. The data serving your fingers is guarded like
  the code.

## Install

1. Download `DRS-Smart-Keyboard-v1.21.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Read the full `PRIVACY.md`.

</div>
