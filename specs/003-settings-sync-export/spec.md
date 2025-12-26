# Feature Specification: 设置页面与数据同步导出

**Feature Branch**: `003-settings-sync-export`
**Created**: 2025-12-25
**Status**: Draft
**Input**: 修改首页tab3的历史页面，在删除全部按钮右侧增加设置按钮，并配套设置页面。包含菜谱、收藏菜单数据的导入导出功能，设计合理的数据格式（JSON/Protobuf），支持WebDAV云同步，配置页面及加密功能。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 进入设置页面 (Priority: P1)

用户希望通过历史页面的顶部栏快速进入设置页面，管理应用数据和同步配置。

**Why this priority**: 设置入口是所有功能的门户，用户必须先能进入设置才能使用导入导出和同步功能。

**Independent Test**: 点击历史页面顶部栏的设置图标，应导航到设置页面。

**Acceptance Scenarios**:

1. **Given** 用户在历史页面, **When** 点击顶部栏右侧的设置图标, **Then** 导航到设置主页面
2. **Given** 用户在设置页面, **When** 点击返回按钮, **Then** 返回历史页面

---

### User Story 2 - 本地文件导出 (Priority: P1)

用户希望将应用中的菜谱和历史数据导出为本地文件，以便备份或迁移到其他设备。

**Why this priority**: 数据导出是用户保护数据的基本需求，无需网络即可完成，是核心功能。

**Independent Test**: 在设置页面选择导出功能，选择导出范围，导出文件到设备存储。

**Acceptance Scenarios**:

1. **Given** 用户在设置页面, **When** 点击"导出数据"按钮, **Then** 显示导出选项（菜谱、历史记录、全部）
2. **Given** 用户选择导出范围, **When** 确认导出, **Then** 生成JSON文件并保存到用户选择的位置
3. **Given** 导出成功, **When** 文件生成完成, **Then** 显示成功提示并显示文件路径
4. **Given** 导出过程中发生错误, **When** 写入失败, **Then** 显示错误信息并提示用户重试

---

### User Story 3 - 本地文件导入 (Priority: P1)

用户希望从本地文件导入之前导出的数据，恢复菜谱和历史记录。

**Why this priority**: 导入是导出的对应功能，完整的备份恢复流程缺一不可。

**Independent Test**: 在设置页面选择导入功能，选择有效的导出文件，数据成功导入应用。

**Acceptance Scenarios**:

1. **Given** 用户在设置页面, **When** 点击"导入数据"按钮, **Then** 打开文件选择器
2. **Given** 用户选择有效的导出文件, **When** 确认导入, **Then** 显示导入预览（将导入的数据条数）
3. **Given** 用户确认导入预览, **When** 执行导入, **Then** 数据成功导入并显示成功提示
4. **Given** 选择的文件格式无效, **When** 解析文件, **Then** 显示格式错误提示
5. **Given** 导入的数据与现有数据有冲突（相同ID）, **When** 执行导入, **Then** 按"新增或更新"策略处理（基于syncId判断）

---

### User Story 4 - WebDAV服务配置 (Priority: P2)

用户希望配置WebDAV服务器信息，以便后续进行云同步。

**Why this priority**: WebDAV配置是云同步的前置条件，但不影响本地导入导出功能的使用。

**Independent Test**: 在设置页面进入WebDAV配置，填写服务器信息并测试连接成功。

**Acceptance Scenarios**:

1. **Given** 用户在设置页面, **When** 点击"WebDAV同步"选项, **Then** 进入WebDAV配置页面
2. **Given** 用户在WebDAV配置页, **When** 填写服务器URL、用户名、密码, **Then** 可以保存配置
3. **Given** 用户填写完配置, **When** 点击"测试连接", **Then** 验证连接并显示结果（成功/失败及原因）
4. **Given** 连接测试成功, **When** 保存配置, **Then** 配置持久化保存（密码加密存储）
5. **Given** 配置已保存, **When** 再次进入配置页面, **Then** 显示已保存的配置（密码脱敏显示）

---

### User Story 5 - WebDAV数据同步 (Priority: P2)

用户希望通过WebDAV将数据同步到云端或从云端恢复数据。

**Why this priority**: 云同步是本地备份的增强功能，提供跨设备同步能力。

**Independent Test**: 配置WebDAV后，执行上传同步，数据成功上传到WebDAV服务器。

**Acceptance Scenarios**:

1. **Given** WebDAV已配置且连接正常, **When** 进入同步页面, **Then** 显示上次同步时间和同步操作选项
2. **Given** 用户点击"上传到云端", **When** 执行上传, **Then** 加密数据后上传到WebDAV服务器
3. **Given** 上传成功, **When** 完成上传, **Then** 更新上次同步时间并显示成功提示
4. **Given** 用户点击"从云端恢复", **When** 执行下载, **Then** 下载并解密数据，显示恢复预览
5. **Given** 用户确认恢复, **When** 执行恢复, **Then** 数据成功导入应用
6. **Given** 云端没有备份数据, **When** 点击"从云端恢复", **Then** 提示云端无数据

---

### User Story 6 - 数据加密设置 (Priority: P2)

用户希望为WebDAV同步设置加密密码，保护云端数据安全。

**Why this priority**: 加密保护用户隐私，但可以使用默认加密策略，用户可选择自定义。

**Independent Test**: 在WebDAV配置中设置加密密码，上传的数据确实被加密。

**Acceptance Scenarios**:

1. **Given** 用户在WebDAV配置页, **When** 查看加密选项, **Then** 显示加密开关和密码设置
2. **Given** 用户启用加密, **When** 设置加密密码, **Then** 密码保存用于后续加解密
3. **Given** 用户已设置加密密码, **When** 上传数据, **Then** 数据使用AES加密后上传
4. **Given** 用户尝试从云端恢复加密数据, **When** 输入正确密码, **Then** 成功解密并恢复
5. **Given** 用户尝试从云端恢复加密数据, **When** 输入错误密码, **Then** 提示密码错误

---

### Edge Cases

- 导出时数据量为0（无菜谱或历史）如何处理？显示提示无数据可导出
- 导入文件损坏或不完整时如何处理？显示解析错误并终止导入
- WebDAV服务器不可达时如何处理？显示网络错误并支持重试
- 导入导出过程中应用被杀死如何处理？下次启动检测未完成的操作
- 多设备同时上传造成冲突如何处理？以最后上传的版本为准（简单策略）
- 加密密码遗忘如何处理？提示用户云端数据无法恢复，只能重新上传

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 在历史页面顶部栏的删除按钮右侧显示设置图标按钮
- **FR-002**: 系统 MUST 提供设置主页面，展示数据管理和同步选项
- **FR-003**: 系统 MUST 支持将菜谱数据导出为JSON格式文件
- **FR-004**: 系统 MUST 支持将历史记录数据导出为JSON格式文件
- **FR-005**: 系统 MUST 支持导出全部数据（菜谱+历史）为单个JSON文件
- **FR-006**: 系统 MUST 支持从JSON文件导入菜谱数据
- **FR-007**: 系统 MUST 支持从JSON文件导入历史记录数据
- **FR-008**: 系统 MUST 在导入前验证文件格式的有效性
- **FR-009**: 系统 MUST 在导入时基于syncId处理数据冲突（存在则更新，不存在则新增）
- **FR-010**: 系统 MUST 提供WebDAV服务器配置页面（URL、用户名、密码）
- **FR-011**: 系统 MUST 支持测试WebDAV连接有效性
- **FR-012**: 系统 MUST 加密存储WebDAV密码
- **FR-013**: 系统 MUST 支持将数据加密后上传到WebDAV服务器
- **FR-014**: 系统 MUST 支持从WebDAV服务器下载并解密数据
- **FR-015**: 系统 MUST 提供加密密码设置功能（用于云端数据加解密）
- **FR-016**: 系统 MUST 使用AES对称加密算法加密上传数据
- **FR-017**: 系统 MUST 记录并显示上次同步时间
- **FR-018**: 系统 MUST 在导入导出过程中显示进度指示

### Key Entities *(include if feature involves data)*

- **ExportData**: 导出数据的封装结构，包含版本号、导出时间、菜谱列表、历史记录列表
- **WebDAVConfig**: WebDAV配置信息，包含服务器URL、用户名、加密后的密码、加密密钥（可选）、上次同步时间
- **SyncMetadata**: 同步元数据，包含同步ID、同步时间戳、数据哈希值

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 用户能在 10 秒内完成设置入口定位并进入设置页面
- **SC-002**: 本地导出 1000 条菜谱数据用时不超过 5 秒
- **SC-003**: 本地导入 1000 条菜谱数据用时不超过 10 秒
- **SC-004**: WebDAV连接测试响应时间在正常网络下不超过 5 秒
- **SC-005**: 云端上传 1000 条数据用时不超过 30 秒（取决于网络）
- **SC-006**: 导入导出成功率达到 99%（排除文件损坏和网络问题）
- **SC-007**: 用户首次配置WebDAV到完成同步的操作步骤不超过 5 步

## Assumptions

- 用户设备有足够的存储空间用于导出文件
- WebDAV服务器支持基本的PROPFIND、GET、PUT操作
- 用户网络环境稳定，能够完成云端同步操作
- 数据量在合理范围内（通常不超过数千条记录）
- 用户理解加密密码的重要性，遗失将无法恢复云端数据
