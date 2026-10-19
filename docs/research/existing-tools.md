# Does this already exist? Research on tech-help tools for elders

Researched 18 Oct 2026 by four parallel researchers (Opus ×2, Sonnet, Haiku; Fable hit a usage limit and was replaced). Claims carry source links. Items marked *unverified* come from model knowledge, not a fetched page.

## Short answer

**Nothing found that does the exact thing:** read the live phone screen, in Hindi/English, and *point at* the next button for the person to tap themselves, across any app. Pieces exist separately:

| What exists | Closest examples | What's missing |
|---|---|---|
| Talks about your screen | Gemini Live screen share (free on Android, speaks Hindi), ChatGPT voice | Doesn't point at buttons; generic, not paced for elders |
| Points where to click | Microsoft Copilot Vision "Highlights" | Windows desktop only, US |
| Points on phone | Stepli (Android, 2026, solo dev) | Only fixed, hand-written guides; English/Urdu; 1 upvote |
| Does the task *for* you | Gemini app automation / "Gemini Intelligence", Siri onscreen awareness, Project Astra | Flagship phones only (Pixel 10, Galaxy S26, iPhone 15 Pro+), US/Korea first, teaches nothing |
| Simplified phone | Pixel Simple View, Samsung Easy mode, Apple Assistive Access, Simpli5 launcher | No guidance at all |
| Indian helpers | AskDiya, Vira Assist (WhatsApp, Hindi/English, ₹299–799/mo) | Chat only, never see the screen |
| Guide apps | Siggi (iOS, English, $8.49, no ratings), Starthilfe (Germany) | Static guides, iOS can't overlay other apps |
| Prototypes | Sahayak (GitHub: Hindi voice + accessibility + Claude, does steps itself) | 1 commit, sideloaded |
| Research | Synapse (IMWUT 2022) tap-guidance from demonstrations | Breaks when app version/layout changes |

Sources: [Gemini Live tips](https://blog.google/products/gemini/gemini-live-android-tips/), [Gemini Live free on Android](https://tomsguide.com/ai/gemini-live-video-and-screen-sharing-is-now-free-for-android-heres-how-to-use-it), [Gemini Live in Hindi](https://www.digit.in/news/general/gemini-live-is-now-available-in-9-indian-languages-including-hindi-everything-announced-at-google-for-india.html), [Gemini camera Visual Guidance](https://blog.google/products/gemini/gemini-live-updates-august-2025/), [Gemini automation](https://techcrunch.com/2026/02/25/gemini-can-now-automate-some-multi-step-tasks-on-android), [Gemini Intelligence](https://www.engadget.com/2170770/gemini-intelligence-brings-app-automation-to-android/), [Copilot Highlights](https://blogs.windows.com/windowsexperience/2025/06/12/copilot-vision-on-windows-with-highlights-now-available-in-us/), [Stepli](https://www.producthunt.com/products/stepli), [Sahayak](https://github.com/harleenkaurbhatia/sahayak), [AskDiya](https://www.askdiya.in/), [Vira](https://getvira.in/), [Siggi](https://apps.apple.com/us/app/id6747631567), [Pixel Simple View](https://www.androidauthority.com/pixel-simple-view-3505692/), [Bixby One UI 8.5](https://news.samsung.com/us/samsung-introduces-new-bixby-one-ui-8-5), [Assistive Access](https://mcmw.abilitynet.org.uk/how-to-simplify-your-iphone-or-ipad-s-interface-using-assistive-access-in-ios-26), [Synapse paper](https://ar5iv.labs.arxiv.org/html/2209.09596), [Simpli5](https://www.digit.in/features/apps/heres-how-simpli5-is-making-smartphones-more-accessible-for-seniors.html).

## Why your parents haven't heard of any of it

1. **Elders don't go looking.** Only 23% of older adults pick internet search to solve tech problems vs 54% of younger people, and only 33% learn by trial and error ([Leung et al. 2012](https://www.cs.ubc.ca/labs/edapt/papers/leung2012.pdf)). So someone else has to find and install the help app, which is the very dependence it's meant to remove.
2. **The family *is* the help system.** ~49% of US adults do online tasks for another adult ([Foong et al. 2025](https://pmc.ncbi.nlm.nih.gov/articles/PMC12360674)); 88% of intergenerational communication in India is still face-to-face ([HelpAge via Deccan Herald](https://www.deccanherald.com/amp/story/india%2Fkarnataka%2Fbengaluru%2F88-seniors-offline-helpage-urges-action-3585484)). Fixing it for them solves today, teaches nothing ([Malmö study](https://techxplore.com/news/2022-09-tech-savvy-kids-granny-granddad.amp)).
3. **The good features are hidden or flagship-only.** Gemini Live is free but you must know it exists and be comfortable sharing your screen. Automation needs a Pixel 10 / Galaxy S26.
4. **Fear of scams.** Screen-reading and remote-access apps are exactly what scammers use. RBI/NPCI warned against AnyDesk ([Outlook Business](https://www.outlookbusiness.com/personal-finance-news/npci-cautions-consumers-against-downloading-anydesk-app-2886)); the Datzbro malware targeted seniors through accessibility permissions ([ThreatFabric](https://www.threatfabric.com/blogs/datzbro-rat-hiding-behind-senior-travel-scams)); US 60+ lost $7.7B to fraud in 2025, up 59% ([FBI](https://www.fbi.gov/news/stories/scammers-target-older-adult-victims)). 45% of Indian seniors struggle to spot scams ([Storyboard18](https://www.storyboard18.com/digital/45-of-indian-seniors-struggle-to-identify-online-fraud-cybercrimes-on-the-rise-58331.htm)).
5. **Programs don't reach them.** India's PMGDISHA covers ages 14–60, rural only ([source](https://anantamias.com/pmgdisha-digital-literacy/)); NGO drives are small. Only 13% of Indians 60+ have used the internet ([ORF/NITI](https://orfonline.org/research/screen-time-may-be-rising-steadily-among-older-indians)).
6. **Content goes stale.** Every guide-based product breaks when apps update (Synapse's own stated limitation).

## Demand signals
- US: 91% of 50+ own a smartphone; **71% are very interested in a tech support service tailored to older users** ([AARP 2025](https://aarp.org/pri/topics/technology/internet-media-devices/2025-technology-trends-older-adults)).
- India: 37% of seniors struggle to understand app features, 24% find interfaces too complex ([Storyboard18](https://www.storyboard18.com/digital/45-of-indian-seniors-struggle-to-identify-online-fraud-cybercrimes-on-the-rise-58331.htm)). HelpAge found slow one-step-at-a-time teaching plus a revisitable guide worked ([HelpAge](https://www.helpage.org/blog/digital-inclusion-for-older-people-the-helpage-india-experiment/)).
- India phone brands 2025: vivo 21%, Samsung 15%, Oppo 13%, Xiaomi 13%, Apple 10% ([Omdia via GSMArena](https://m.gsmarena.com/vivo_led_indias_smartphone_market_in_2025_with_doubledigit_shipment_growth-amp-71181.php)): four different Android skins to handle.

## Platform constraints
- Google Play lets non-disability apps use the Accessibility API only with in-app disclosure, explicit consent and approval; AI that autonomously plans and acts is banned unless a verified disability tool ([Play policy](https://support.google.com/googleplay/android-developer/answer/10964491?hl=en)). Android 13+ blocks accessibility access for sideloaded apps ([Esper](https://www.esper.io/blog/android-13-sideloading-restriction-harder-malware-abuse-accessibility-apis)).
- iOS: no overlays on other apps, so iPhone gets guides only.

## Lessons for our design
1. **Point, don't do.** The open gap is "teach me", not "do it for me", and it's what Play policy allows anyway. Risk to watch: if Gemini gets good at just doing tasks on cheap phones, teaching demand shrinks.
2. **Trust is the product.** Play Store only, on-device by default, never on banking/UPI screens, no remote control, open source, and include anti-scam tips so it reads as protection.
3. **The adult child is the channel.** Child installs and sets up, can send a guide remotely or get a "stuck" alert; elder does the tapping. Stepli testers asked for exactly this, plus replay and slower steps.
4. **Live screen understanding with recipe hints**, not a screenshot database, so it survives app updates. Start with top apps × top 4 brands.
5. **Hindi voice, elder pacing, easy-to-hard task list** with early wins: things Gemini doesn't offer.
