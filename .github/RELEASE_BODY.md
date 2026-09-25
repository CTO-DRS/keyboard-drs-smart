<div align="center">

# DRS Smart Keyboard V 1.22.0

## الجولة الشاملة الثانية والعشرون — الحدود الصادقة: سحب المهام المثبتة، سقف للتثبيتات، بريسيتات لوحات DRS، وإحياء CTRL/ALT الميتة

**Twenty-Second Comprehensive Round — Honest Bounds: Drag-Reorder the Pinned Tasks, a Cap for the Pins, DRS Layout Presets, and the Dead CTRL/ALT Keys Revived**

<img src="https://raw.githubusercontent.com/CTO-DRS/keyboard-drs-smart/main/docs/images/hero.png" width="100%"/>

</div>

<div dir="rtl">

## نبذة عن المشروع

**DRS Smart Keyboard** لوحة مفاتيح أندرويد حرة ومفتوحة المصدر (Apache-2.0)،
مبنية بـ Kotlin وJetpack Compose وMaterial 3، صُممت **عربية أولًا**: واجهة
عربية كاملة بدعم RTL أصيل، تخطيطات عربية مدروسة، واقتراحات وتصحيح عربي — مع
إنجليزية كخيار ثانٍ كامل. كل ما تكتبه يبقى على جهازك: لا حسابات، لا تتبع،
لا إعلانات.

## الجديد في V 1.22.0 — جولة الحدود الصادقة

فحص ثلاثي جديد تحقق فيه من كل ادعاء بالكود الفعلي سطرًا سطرًا: خمسة مرشحات
مؤكدة أُصلحت حتى النهاية، وادعاء واحد انهار أمام الدليل (ملفات western.json
نظيفة تمامًا من العربية) فتحول إلى عقد حماية دائم بدل «إصلاح» زائف. كل بند
أدناه عمل حقيقي مُختبر، لا ترقيم أسطر.

- ↔️ **إعادة ترتيب المهام المثبتة بالسحب** — شريط المهام العشرة كان يُرتَّب
  بأسهم «أعلى/أسفل» تنقل المهمة خانة واحدة في كل ضغطة عبر قائمة الكتالوج
  الكاملة (والسحب الموجود في المستودع كان لمحرر الإجراءات السريعة — ميزة
  أخرى لا علاقة لها). الآن: **اضغط مطولًا على مهمة مثبتة في الدرج واسحبها**
  — كل تجاوز لمهمة أخرى تبديل حقيقي عبر `reorderPinnedTool`، المرسوم يتبع
  إصبعك بشفافية خفيفة، والترتيب يكتب في نفس المخزن المحفوظ الذي تكتبه كل
  عناصر التحكم فيبقى بعد إعادة التشغيل. أزرار الأسهم باقية لـTalkBack
  وللضبط الدقيق، ولا شيء في المسار يصنع دبوسًا حادي عشر أو خانة مكررة —
  النواة النقية `reorderPinnedSlots` تقصّ وتنقّي وترفض خارج النطاق بصمت
  صادق.

- 📌 **سقف صادق لتثبيتات الحافظة («سقف التثبيتات»)** — حدّ سجل الحافظة
  يستثني المثبتة صراحةً (وهذا صحيح — دبابيسك لا تمحوه عمليات التنظيف)، لكن
  النتيجة أن المثبتة كانت **المخزن الوحيد بلا أي سقف** في الحافظة كلها:
  `pinClip` كان يكتب في Room بلا سؤال واحد. الآن سقف قابل للضبط
  (5–200، الافتراضي 50) عبر مفتاح `clipboard__pinned_max_size` في شاشة
  إعدادات الحافظة: عند بلوغ السقف يُرفض التثبيت الجديد **بتوست صادق**
  («سقف التثبيتات بلغ حده — ألغِ تثبيت عنصر لإضافة غيره») في اللوحة
  ومسار «تثبيت العنصر النشط» معًا. والقرار النقي `pinCapAllows` يحفظ
  الحدود الثلاثة: إعادة تثبيت عنصر مثبت تنجح دائمًا، والتثبيتات الموجودة
  فوق سقف منخفض **لا تُحذف أبدًا** — السقف يبوّب الجديد فقط.

- 🗂️ **لوحات DRS تدخل كتالوج البريسيتات أخيرًا** — حزمة
  `org.drs.layouts.drs` (نمط الحاسوب PC 102، والنمط المغاربي، وصفحة الرموز
  العربية) كانت تُدمج في محرك التخطيطات وتظهر في محرر الأنواع اليدوي فقط،
  بينما **قائمة البريسيتات المقترحة لم ترها أبدًا**: صفر إشارة له في 73
  بريسيتًا. الحزمة تنشر الآن `subtypePresets` خاصة بها: **ar-MA** (المغاربي
  + الدرهم المغربي + صف أرقام غربي — فالمغرب العربي يكتب بأرقام لاتينية)،
  **ar-DZ** (المغاربي + الدينار الجزائري + أرقام غربية)، و**ar-SA** (نمط
  الحاسوب + الريال السعودي + صف الأرقام الهندية). كل مرجع (لوحة، مؤلِّف،
  عملة، خرائط نوافذ) تحقق يدويًا عبر قراءة الأصول نفسها، وعقد اختبار يمنع
  أي تصادم وسوم مع كتالوج التوطين ويمنع أي مرجع مكسور مستقبلًا.

- ⌨️ **مفاتيح CTRL/ALT تنبض بعد 22 جولة من الموت** — منذ اليوم الأول كان
  الضغط على CTRL أو ALT (إن أعلنهما تخطيط ما) يسقط في فرع «مفتاح مجهول»:
  `flogError("Received unknown key")` ورسم فقط — لا حالة موديفاير في
  `KeyboardState` إطلاقًا. الإحياء الكامل: حالة لمبة جديدة
  (`InputModifierState`: OFF/LATCHED/LOCKED) في منطقتي بتتين حرتين من سجل
  الحالة، ونابض يقلّب الضغطة عاديًا (لمسة = لمبة واحدة تنطفي بعد الاستهلاك،
  لمستان = قفل يبقى حتى لمسة ثالثة، وأكواد `CTRL_LOCK/ALT_LOCK` تقفل مباشرة)،
  والاستهلاك الحقيقي: **سهم مع CTRL = قفزة كلمة**، **سهم مع ALT = قفزة
  سطر/صفحة** (نفس دلالات META_CTRL_ON/META_ALT_ON التي كان المحرك يحملها
  لمفاتيح قفز الكلمات الصريحة)، و**حذف مع CTRL = حذف كلمة** للأمام والخلف،
  بينما أي مفتاح آخر يستهلك اللمبة فلا تفاجئك لاحقًا. والمفاتيح أصبحت
  **قابلة للوصول**: مدخلان جديدان في كتالوج شريط التقني (`Ctrl`/`Alt`)
  مع نقطة الحالة الحية التي تقرأ اللمبة الفعلية — الحقيقة على البلاطة
  لا اختصار مجاني.

- 🧪 **الحماية قبل الانهيار: عقد western.json النظيف** — ادعاء الفحص «تسميات
  عربية RTL في western.json» **انهار أمام المسح**: الملفان نظيفان 100% من
  أي محرف عربي (المقاطع `rtl` الموجودة هي انعكاس أقواس ترقيم مشروع، والمحرف
  العربي الوحيد في العائلة «٪» داخل western_samsung.json المقصود تصميمًا
  للمستخدم العربي المفضل للنمط الغربي). الادعاء المرفوض صار **عقد بنية
  أصول دائمًا**: إن تسللت تسمية عربية إلى صفحتي الرموز الغربية يومًا فالبناء
  سيسقط قبل النشر — لا صمت إصدارًا.

- 🏗️ **التشغيل اليدوي للنشر لا يرمي أثره** — `workflow_dispatch` في مسار
  الإصدار كان يبني APK وAAB موقّعين ثم يرميهما مع الـrunner: لا نشر (النشر
  حكر على وسم) ولا رفع أرتيفاكت أصلًا. التشغيل اليدوي يحتفظ الآن بالثنائيات
  الموقعة كأرتيفاكت مسار (`drs-signed-release`، صلاحية 14 يومًا).

- 🧹 **TODO رامٍ يُحذف** — `getLanguagePackIdPref(): Nothing = TODO(...)` في
  شاشة مدير حزم اللغات كان قنبلة تتهدد أي استدعاء مستقبلي — دالة ميتة
  بموت مؤكد إن دُعيت. حُذفت.

- 🧪 **23 اختبار وحدة جديدًا (544 ناجحة، كانت 521)** — عقود `reorderPinnedSlots`
  الكاملة (الأمام والخلف وخارج النطاق واللامعمل وعدم تلفيق دبابيس أو تكرارات
  فوق سقف العشرة)، عقود `pinCapAllows` الأربعة (تحت السقف، عند السقف، إعادة
  التثبيت، السقف المنخفض لا يدمّر)، دورة النابض الكاملة باتجاهها الثلاثة
  والقفل المباشر والاستهلاك الصادق، `fromInt` ذهابًا وإيابًا مع الرداءة
  للقيم الفاسدة، **عقد عدم تصادم بتّي** في سجل الحالة (CTRL/ALT/SHIFT
  تعيش معًا)، كتالوج شريط التقني بأكواده الحقيقية وفرادة معرّفاته، خريطة
  `toggleStateOf` للنقطة الحية، و**عقود الأصول الثلاثة**: بريسيتات DRS
  (بنيتها ومراجعها المدمجة وفرادة وسومها) وwestern.json النظيف من العربية.

- 🌐 **التوازي الآن 2,393 مفتاحًا لكل لغة (AR/EN)** — أربعة مفاتيح جديدة
  (توست السقف، عنوان الإعداد، تلميح السحب) عبر سكربت `add_v1220_strings.py`
  بالنمط المرجعي v1210: idempotent مع بوابة توازٍ تسقط عند أي انفراد.

</div>

<div dir="ltr">

## About the project

**DRS Smart Keyboard** is a free, open-source Android keyboard
(Apache-2.0) built with Kotlin, Jetpack Compose and Material 3 —
**Arabic-first**: a fully Arabic interface with native RTL support,
thoughtful Arabic layouts and Arabic suggestions/correction, with English
as a complete second option. Everything stays on your device: no accounts,
no tracking, no ads.

## What's new in V 1.22.0 — The Honest Bounds Round

A fresh tri-front audit verified every claim against the actual code,
line by line: five candidates confirmed and fixed to the end, and one
claim that collapsed under evidence (the western.json files are 100%
free of Arabic labels) — turned into a permanent asset contract instead
of a fake "fix". Every item below is real, tested work.

- ↔️ **Drag to reorder the pinned tasks** — the ten-slot tasks bar was
  reorderable only by one-slot up/down nudges routed through the whole
  catalogue order (and the drag that existed in the repo belonged to the
  quick-actions editor — a different feature). Now: **long-press a pinned
  task in the drawer and drag** — every crossing is a real swap through
  `reorderPinnedTool`, the dragged row follows your finger with a subtle
  translucency, and the order is written to the same persisted store every
  other control uses, surviving restarts. The up/down buttons stay for
  TalkBack and precise nudging, and nothing in the path can fabricate an
  11th pin or a duplicate slot — the pure core `reorderPinnedSlots`
  dedupes, caps and refuses out-of-range moves.

- 📌 **An honest cap for clipboard pins («سقف التثبيتات»)** — the history
  size limit explicitly exempts pinned items (by design — your pins must
  survive cleanup), but the consequence was that pinned items were **the
  only unbounded store in the entire clipboard**: `pinClip` wrote to Room
  without asking a single question. The cap is now configurable
  (5–200, default 50) via `clipboard__pinned_max_size` in the clipboard
  settings screen: at the cap a new pin is refused **with an honest
  toast** ("Pin cap reached — unpin an item to pin another") from both
  the panel popup and the pin-active path. The pure decision
  `pinCapAllows` keeps all three bounds: re-pinning an already-pinned
  item always succeeds, existing pins above a lowered cap are **never
  auto-destroyed**, and only new pins are gated.

- 🗂️ **DRS layouts finally join the preset catalog** — the
  `org.drs.layouts.drs` package (PC 102, Maghreb, and the Arabic symbols
  page) was merged into the layout engine and reachable through the manual
  subtype editor, but the **suggested presets list never saw it**: zero
  references across all 73 presets. The package now publishes its own
  `subtypePresets`: **ar-MA** (Maghreb layout + Moroccan dirham + a
  western digit row — the Maghreb writes in Latin digits), **ar-DZ**
  (Maghreb + Algerian dinar + western digits), and **ar-SA** (PC 102 +
  Saudi riyal + the Eastern Arabic numeral row). Every reference (layout,
  composer, currency set, popup mapping) was verified by reading the
  assets themselves, and a test contract prevents tag collisions with the
  localization catalog or broken references forever.

- ⌨️ **CTRL/ALT keys pulse again after 22 dead rounds** — since day one,
  pressing CTRL or ALT (should any layout declare them) fell into the
  "unknown key" branch: `flogError("Received unknown key")` and nothing
  else — no modifier state existed anywhere in `KeyboardState`. The full
  revival: a new latch state (`InputModifierState`: OFF/LATCHED/LOCKED)
  in two free 2-bit regions of the state register, a cycle like shift
  (tap = one-shot latch consumed by the next key, tap again = lock until
  a third tap, `CTRL_LOCK/ALT_LOCK` codes jump straight to lock), and the
  real consumption: **CTRL + arrow = word jump**, **ALT + arrow =
  line/page jump** (the same META_CTRL_ON/META_ALT_ON semantics the
  engine already carried for the explicit word-jump keys), and
  **CTRL + delete = word deletion** in both directions — while any other
  key consumes a one-shot latch so it can never surprise you later. The
  keys are now **reachable**: two new entries in the tech-toolbar
  catalogue (`Ctrl`/`Alt`) with a live active dot reading the actual
  latch — the truth on the tile, not a free pass.

- 🧪 **Protection before the crash: the western.json contract** — the
  audit claim "Arabic RTL labels in western.json" **collapsed under the
  scan**: both files are 100% free of Arabic characters (the `rtl`
  segments present are proper bracket-mirroring selectors, and the only
  Arabic char in the family is the «٪» popup inside western_samsung.json —
  deliberate design for the Arabic user preferring the western layout).
  The refuted claim is now a **permanent asset-structure contract**: if
  an Arabic label ever sneaks into the western symbol pages, the build
  drops before release — no silent regressions.

- 🏗️ **Manual release runs keep their artifacts** — `workflow_dispatch`
  in the release path built a signed APK and AAB and then threw both away
  with the runner: no publish (that is tag-only) and no artifact upload
  at all. Manual runs now keep the signed binaries as workflow artifacts
  (`drs-signed-release`, 14-day retention).

- 🧹 **A live TODO removed** — `getLanguagePackIdPref(): Nothing =
  TODO(...)` in the language-pack manager screen was a bomb aimed at any
  future caller — a dead function with a guaranteed death if invoked.
  Removed.

- 🧪 **23 new unit tests (544 passing, was 521)** — the full
  `reorderPinnedSlots` contracts (forward/backward, out-of-range, no-op,
  no fabricated pins or duplicates above the cap of ten), the four
  `pinCapAllows` contracts (under cap, at cap, re-pin, lowered cap never
  destroys), the full latch cycle in all three directions plus direct
  lock and honest consumption, `fromInt` round-trips with garbage
  fallback, a **no-bit-collision contract** in the state register
  (CTRL/ALT/SHIFT coexist), the tech-toolbar catalogue with real codes
  and unique ids, the `toggleStateOf` live-dot map, and **three asset
  contracts**: the DRS presets (structure, merged references, unique
  tags) and the Arabic-free western symbol pages.

- 🌐 **Parity is now 2,393 keys per language (AR/EN)** — four new keys
  (cap toast, setting title, drag hint) via the v1210-reference-pattern
  `add_v1220_strings.py`: idempotent with a parity gate that fails on any
  orphan.

## Install

1. Download `DRS-Smart-Keyboard-v1.22.0.apk` below.
2. Install it (allow unknown sources when prompted).
3. Open Settings → System → Languages & input → enable the keyboard.
4. Pick it as your default keyboard and start typing.

Verify integrity with the SHA-256 checksums in the attached `SHA256SUMS.txt`.

## Privacy

Everything runs **locally and offline**: no accounts, no tracking, no ads,
no network except the app's own update check. Read the full `PRIVACY.md`.

</div>
