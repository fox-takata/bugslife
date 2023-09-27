# TaxType 設計

## 概要

税率周りの管理を行う

### アクション

- 一覧
- 照会
- 作成
- 更新
- 削除

### 要件

- 税率タイプは、税率、税込みかどうか、端数処理方法を持つ
- 税込かどうかは、true か false をもつ
- 端数処理方法には、floor か round か ceil をもつ
- 削除の際
  - その ID を他で使用している場合は削除ができない

## モデル

- TaxType

| `tax_type`   | Type    | Required | memo |
| ------------ | ------- | :------: | ---- |
| id           | Long    |    ○     |      |
| rate         | Integer |    ○     |      |
| rounding     | Boolean |    ○     |      |
| tax_included | Integer |    ○     |      |
| name         | String  |          | TEXT |

| ID  | Rate(%) | Tax included | Rounding |
| --- | ------- | ------------ | -------- |
| 1   | 0       | No           | Floor    |
| 2   | 0       | No           | Round    |
| 3   | 0       | No           | Ceil     |
| 4   | 0       | Yes          | Floor    |
| 5   | 0       | Yes          | Round    |
| 6   | 0       | Yes          | Ceil     |
| 7   | 8       | No           | Floor    |
| 8   | 8       | No           | Round    |
| 9   | 8       | No           | Ceil     |
| 10  | 8       | Yes          | Floor    |
| 11  | 8       | Yes          | Round    |
| 12  | 8       | Yes          | Ceil     |
| 13  | 10      | No           | Floor    |
| 14  | 10      | No           | Round    |
| 15  | 10      | No           | Ceil     |
| 16  | 10      | Yes          | Floor    |
| 17  | 10      | Yes          | Round    |
| 18  | 10      | Yes          | Ceil     |
