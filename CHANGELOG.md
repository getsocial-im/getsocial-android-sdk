# GetSocial Android SDK

## Version History

### v5.3.2

+ FIXED NullPointerException when null onWindowStateChangeListener was set and window opened. 

---

### v5.3.1

+ ADDED onInviteFriendsFailure(InviteFriendsException exception) callback to InviteFriendsListener. Now friend invitation will fail if Smart Invite Url was not retrieved. 

---

### v5.3.0

+ ADDED Polish and Ukrainian localizations
+ FIXED Proguard rules to decrease methods count

---

### v5.2.2

+ FIXED Compile warnings Ignoring InnerClasses attribute...

---

### v5.2.1

+ FIXED Send chat message after adding an identity

---

### v5.2.0

+ ADDED KakaoTalk invite plugin
+ ADDED Social graph

---

### v5.1.5

+ FIXED RejectedExecutionException in communication layer

---

### v5.1.4

+ FIXED UI bugs

---

### v5.1.3

+ FIXED Send chat message or activity post on Enter button click
+ FIXED GetSocial window size calculation on Android N
+ FIXED OnReferralDataReceivedListener not been called

---

### v5.1.2

+ FIXED Crash when attempting disconnect while connecting 

---

### v5.1.1

+ FIXED corner case NullPointerException when receiving referral data from Facebook


---

### v5.1.0

+ ADDED Chat API to build custom chat views
+ ADDED system share provider to Smart Invites
+ ADDED Facebook Messenger as Smart Invites provider
+ ADDED Indonesian, Tagalog, Malay, Brazilian Portuguese and Vietnamese localization
+ REMOVED dependency on io.reactivex:rxandroid and io.reactivex:rxjava
+ DEPRECATED method `GetSocial.getCurrentUser().getIdentities()`, use `GetSocial.getCurrentUser().getAllIdentities()` instead
+ DEPRECATED interface `UpdateUserInfoObserver`, use `OperationVoidCallback` instead
+ DEPRECATED method `GetSocialChat.setOnUnreadConversationsCountChangedListener(...)`, use `GetSocialChat.addOnUnreadRoomsCountChangedListener(...)` instead
+ DEPRECATED method `GetSocialChat.getUnreadConversationsCount(...)`, use `GetSocialChat.getUnreadPrivateRoomsCount()` and `GetSocialChat.getUnreadPublicRoomsCount()` instead


---

### v5.0.10

+ REDUCED SDK method count
+ REMOVED dependency on `io.reactivex:rxandroid` and `io.reactivex:rxjava`
+ FIXED sending invites from background thread with image attachment


---

### v5.0.9

+ FIXED Bug in Analytics

---

### v5.0.8

+ FIXED Bug in save state

---

### v5.0.7

+ FIXED Bugs in Analytics


---

### v5.0.6

+ ADDED support for delay SDK initialization



---

### v5.0.5

+ FIXED Incorrect font scaling when setting text style programmatically through UI config transaction

---

### v5.0.4

+ FIXED bugs related to init from multiple threads

---

### v5.0.3

+ FIXED Duplicate resources issue during build

---

### v5.0.2

+ FIXED Chat notification crash

---

### v5.0.1

+ IMPROVED image loading, memory management and caching
+ IMPROVED SDK initialization with poor internet connectivity
+ IMPROVED offline support
+ IMPROVED support for shared libraries
+ FIXED counters
+ FIXED Occasional connectivity issues with Unity
+ REDUCED SDK method count




---

### v5.0.0

+ ADDED `GetSocialCurrentUser` object
+ ADDED methods to set `DisplayName` and `AvatarUrl` for the current user
+ ADDED methods to add/remove identities for the current user with conflict support
+ ADDED methods to reset the current user
+ ADDED `OnUserActionPerformHandler` to intercept all user interactions
+ ADDED CoreProperty.HEADER_PADDING_TOP and CoreProperty.HEADER_PADDING_BOTTOM
+ DEPRECATED CoreProperty.TITLE_MARGIN_TOP (use CoreProperty.HEADER_PADDING_TOP instead)
+ ADDED `CoreProperty.ACTIVITY_COMMENT_BG_COLOR` property to configure comments background color
+ REMOVED `WRITE_EXTERNAL_STORAGE` permission
+ IMPROVED UI scaling in landscape mode
+ FIXED app install and smart invite sent analytics being reported incorrectly
+ FIXED UI Overlay image not being displayed
+ FIXED styling for popup dialogs in chat list and chat views
+ FIXED smart invites not working when user is logged out and device has no internet connection
+ FIXED `window.overlay-image` UI configuration property not working



---

### v4.0.4

+ FIXED `GetSocial.getSupportedInviteProviders()` method call returning 0 providers when invoked from `GetSocial.init() onSuccess()` callback

---

### v4.0.3

+ ADDED 5 new languages: Icelandic, Korean, Japanese, Chinese Simplified, Chinese Traditional

+ IMPROVED invite flow via email. Now SDK shows only email clients
+ FIXED issue with being not possible to install two apps with GetSocial SDK v4.0.2 integrated
+ FIXED property `ACTIVITY_ACTION_BUTTON_TEXT_Y_OFFSET_NORMAL` and `ACTIVITY_ACTION_BUTTON_TEXT_Y_OFFSET_PRESSED` behaviour



---

### v4.0.2

+ ADDED ability to load json configuration file from absolute path
+ ADDED ability to specify absolute path as a base assets path from the code
+ IMPROVED behaviour of attached images on some Smart Invite providers
+ FIXED null pointer exception when opening chat view from notification on application cold start

---

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
