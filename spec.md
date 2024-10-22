
# 個人資產管理系統規格書

## 1. 產品目的

開發一款簡單的個人資產管理 Android 應用程式，讓使用者能夠快速記錄和管理個人物品，支援條碼掃描快速添加物品，並且可以自定義物品的類型和屬性。

## 2. 基礎功能

### 2.1 物品管理
- 新增物品（手動輸入）
- 編輯物品
- 刪除物品
- 查看物品列表（支援分頁加載和虛擬滾動）

### 2.2 條碼掃描
- 支援 barcode 和 QR code 掃描
- 通過掃描快速新增物品

### 2.3 物品類型
- 預設類型：電子產品、收藏品、其他
- 支援用戶自定義新類型

## 3.資料模型

### 1. Item（物品）
- id: Integer (主鍵)
- name: String (物品名稱，最多 100 字)
- category_id: Integer (外鍵，關聯到 ItemCategory)
- code_type: String (條碼類型，可為空)
- code_content: String (條碼內容，可為空)
- code_image_id: Integer (外鍵，關聯到 Image，代表條碼圖片)
- cover_image_id: Integer (外鍵，關聯到 Image，代表物品封面圖片)
- image_ids: List<Integer> (外鍵，關聯到 Image，代表物品其他圖片)
- created_at: Timestamp
- updated_at: Timestamp

### 2. ItemCategory（物品類別）
- id: Integer (主鍵)
- name: String (類別名稱，如"電子產品"、"收藏品"等)
- created_at: Timestamp
- updated_at: Timestamp

### 3. CategoryAttribute（類別屬性）
- id: Integer (主鍵)
- category_id: Integer (外鍵，關聯到 ItemCategory)
- name: String (屬性名稱，如"品牌"、"價格"等)
- is_required: Boolean (是否為必填屬性)
- is_editable: Boolean (是否可編輯，如"名稱"屬性可能不可編輯)
- value_type: String (值類型，如"text", "number", "date"等)
- default_value: String (預設值，可為空)
- created_at: Timestamp
- updated_at: Timestamp

### 4. ItemAttributeValue（物品屬性值）
- id: Integer (主鍵)
- item_id: Integer (外鍵，關聯到 Item)
- attribute_id: Integer (外鍵，關聯到 CategoryAttribute)
- value: String (屬性值)
- created_at: Timestamp
- updated_at: Timestamp

### 5. Image（圖片）
- id: Integer (主鍵)
- file_path: String (圖片檔案路徑)
- item_id: Integer (外鍵，關聯到 Item)
- order: Integer (相同 item 中圖片順序)
- content: String (圖片敘述)
- created_at: Timestamp
- updated_at: Timestamp

## 4. 使用流程

1. 啟動應用程式
2. 主頁面（物品列表）
   - 頂部搜索欄
   - 新增物品按鈕（固定在右下角，圓形中間是 + 號的圖標）
   - 物品列表（支援滑動和下拉刷新）
3. 新增物品
   - 選擇手動輸入或掃描條碼
   - 填寫物品資訊
   - 選擇性上傳照片
   - 儲存物品
4. 查看物品詳情
5. 編輯物品
6. 刪除物品

## 5. 未來功能 Backlog

1. 用戶管理系統
2. 雲端同步和備份
3. 多平台支援
4. 高級統計和報告功能
5. 自定義條碼識別和資料填充規則
6. 物品詳細資訊管理頁面
7. 設置頁面
8. 表單驗證
   1. 實時驗證：在用戶輸入時就進行驗證，並在輸入框下方顯示錯誤消息。 
   2. 禁用保存按鈕：當表單無效時，使保存按鈕變灰並禁用。 
   3. 錯誤摘要：在表單頂部或底部顯示所有錯誤的摘要。


# 檔案架構
```
com.example.itemanagerv2/
├── data/local/
│   ├── model/
│   │   └── ItemWithImages.kt
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── dao/
│   │   │   ├── ItemDao.kt
│   │   │   ├── ItemCategoryDao.kt
│   │   │   ├── CategoryAttributeDao.kt
│   │   │   ├── ItemAttributeValueDao.kt
│   │   │   └── ImageDao.kt
│   │   └── entity/
│   │       ├── Item.kt
│   │       ├── ItemCategory.kt
│   │       ├── CategoryAttribute.kt
│   │       ├── ItemAttributeValue.kt
│   │       └── Image.kt
│   └── repository/
│       ├── ItemRepository.kt
│       ├── CategoryRepository.kt
│       └── ImageRepository.kt
├── di/
│   └── AppModule.kt
├── ui/
│   ├── theme/
│   │   └── Theme.kt
│   ├── components/
│   │   ├── CustomTopAppBar.kt
│   │   └── ItemCard.kt
│   ├── screens/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── itemdetail/
│   │   │   ├── ItemDetailScreen.kt
│   │   │   └── ItemDetailViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   └── MainActivity.kt
└── util/
    └── DatabaseMigrations.kt
```