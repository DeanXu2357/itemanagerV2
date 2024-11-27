# ItemManager V2

個人資產管理 Android 應用程式，讓使用者能夠快速記錄和管理個人物品。支援條碼掃描快速添加物品，並且可以自定義物品的類型和屬性。

[English Documentation](README.md)

## 功能特點

- 物品管理（新增、編輯、刪除、查看）
- 條碼掃描（支援 barcode 和 QR code）
- 自定義物品類型和屬性
- 圖片管理（物品封面圖、條碼圖片、多張物品圖片）
- 分頁加載和虛擬滾動列表

## 技術架構

- **開發語言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架構模式**: MVVM (Model-View-ViewModel)
- **依賴注入**: Dagger Hilt
- **本地數據庫**: Room Database
- **異步處理**: Kotlin Coroutines & Flow

## 專案結構

```
com.example.itemanagerv2/
├── data/                    # 數據層
│   ├── local/              # 本地數據相關
│   │   ├── dao/           # 數據訪問對象
│   │   ├── entity/        # 數據實體類
│   │   └── model/         # 數據模型
│   ├── manager/           # 業務邏輯管理器
│   └── repository/        # 數據倉庫
├── di/                     # 依賴注入
├── ui/                     # UI 層
│   ├── component/         # UI 組件
│   └── theme/            # 主題相關
└── viewmodel/             # ViewModel 層
```

## 數據模型

### 主要實體

1. **Item**: 物品基本信息
2. **ItemCategory**: 物品類別
3. **CategoryAttribute**: 類別屬性定義
4. **ItemAttributeValue**: 物品屬性值
5. **Image**: 圖片資源

## 開發環境設置

1. 安裝必要工具：
   - Android Studio Arctic Fox 或更新版本
   - JDK 11 或更高版本
   - Android SDK 31 或更高版本

2. 克隆專案：
   ```bash
   git clone [repository-url]
   ```

3. 在 Android Studio 中打開專案

4. 同步 Gradle 文件

5. 運行專案
   - 選擇目標設備（模擬器或實體設備）
   - 點擊 "Run" 按鈕

## 開發指南

### 添加新功能

1. 在相應的 package 中創建所需的類
2. 遵循 MVVM 架構模式
3. 使用依賴注入管理依賴關係
4. 編寫單元測試

### 數據庫操作

- 使用 Room DAO 進行數據庫操作
- 在 Repository 層處理數據邏輯
- 通過 ViewModel 向 UI 層提供數據

### UI 開發

- 使用 Jetpack Compose 創建 UI 組件
- 遵循 Material Design 指南
- 確保適配不同屏幕尺寸

## 待開發功能

1. 用戶管理系統
2. 雲端同步和備份
3. 多平台支援
4. 高級統計和報告功能
5. 自定義條碼識別規則
6. 物品詳細資訊管理
7. 設置頁面
8. 表單驗證增強

## 授權

[授權信息待補充]
