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
