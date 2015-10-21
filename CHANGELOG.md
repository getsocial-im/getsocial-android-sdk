# GetSocial SDK for Android

## Version History

### v4.0.1

+ ADDED option to disable animations to prevent crashes due to Hardware Acceleration on Kindle devices
+ IMPROVED install attribution for latest Facebook SDK 4.7

---

### v4.0.0

Release v4.0.0 brings a lot of new features, improvements and few breaking changes. The most notable are:

+ Integration guide and reference migrated to [docs.getsocial.im](http://docs.getsocial.im).
+ Now GetSocial is split into `Core` (Activity Feed, Notifications, Smart Invites) and `Chat` modules. [Learn more about migration from version 3.x.](http://docs.getsocial.im/#upgrade-guide)
+ New UI configuration system. Now all UI properties can be customized via JSON configuration file. [Learn more...](http://docs.getsocial.im/ui-customization/#developers-guide)
+ Big part of `GetSocial` methods were refactored to more meaningful names (e.g. `authenticateGame(...)` => `init(...)`, `verifyUserIdentity(...)` => `login(...)`, etc.)/   
+ Added support for linking multiple user accounts. [Learn more...](http://docs.getsocial.im/#adding-identity-info)
+ Replaced generic `open(...)` view method with sophisticated builders API. To obtain view builder call `createXxxView()`. 
+ Added friends list view. [Learn more...](http://docs.getsocial.im/#friends-list)
+ Added support for Facebook SDK v4.x. [Learn more...](http://docs.getsocial.im/#integration-with-facebook)
+ Now users can receive notifications about likes and comments from application account.





---

### v3.5.6

+ FIXED issue with Cache.getInstance returned null when authenticating game.

---

### v3.5.5
+ Bug fixes

---

### v3.5.4
+ Bug fixes

---


### v3.5.3
+ ADDED support for FB SDK 4.x tracking of smart invites

---


### v3.5.2
+ Bug fixes

---


### v3.5.1
+ ADDED user content moderation callback

---


### v3.5.0
+ ADDED support for deeplinking
+ ADDED invite data bundling

---


### v3.4.0
+ Support for activities with image, button, action
+ onGameAvatarClick handler
+ onActivityActionClick handler
+ Leaderboard data exposed to developer
+ Chat Kill switch

---


### v3.3.0
+ Global Chat Room

---


### v3.2.0
+ Updated chat engine
+ Small improvements

---


### v3.1.1
+ Small bug fixes

---

### v3.1.0 
+ Initial GetSocial version