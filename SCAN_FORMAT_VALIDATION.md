# 扫码格式验证功能说明

## 功能概述

实现了扫码后的数据格式验证和处理功能，支持两种格式：
1. **纯 IP 格式**：`192.168.0.1`
2. **IP:Port 格式**：`192.168.0.1:3000`

## 核心文件

### 1. IpPortValidator.kt
**位置**：`app/src/main/java/com/dragon/videorecorder/utils/IpPortValidator.kt`

**功能**：
- 验证纯 IPv4 地址格式
- 验证 IP:Port 格式
- 解析输入字符串，提取 IP 和端口
- 提供详细的错误提示信息

**主要方法**：
```kotlin
// 验证纯 IP 地址
isValidIp(ip: String): Boolean

// 验证 IP:Port 格式
isValidIpPort(input: String): Boolean

// 解析输入，返回 Pair<IP, Port?>
parseIpPort(input: String): Pair<String, Int?>?

// 验证并解析，返回完整结果
validateAndParse(input: String): ValidationResult
```

### 2. MainViewModel.kt
**位置**：`app/src/main/java/com/dragon/videorecorder/viewmodel/MainViewModel.kt`

**修改内容**：
- 重构 `addDevice()` 方法，支持格式验证和自动端口设置
- 新增 `getDeviceDisplayText()` 方法，返回格式化的 IP:Port 显示文本

**功能**：
- 自动识别输入格式（纯 IP 或 IP:Port）
- 如果提供端口，自动更新 RTP 端口设置
- 格式错误时显示详细的错误提示
- 添加设备成功时显示完整的 IP:Port 信息

### 3. DeviceMenu.kt
**位置**：`app/src/main/java/com/dragon/videorecorder/ui/main/DeviceMenu.kt`

**修改内容**：
- 在设备列表中显示格式化的 IP:Port 信息
- 修改 `DeviceItem` 组件，显示完整的设备信息

### 4. MainScreen.kt
**位置**：`app/src/main/java/com/dragon/videorecorder/ui/main/MainScreen.kt`

**修改内容**：
- `StatusIndicator` 组件新增 `currentPort` 参数
- 在连接状态界面显示 IP 的同时显示端口
- 格式：`• 192.168.0.1:3000`

## 使用流程

### 扫码添加设备流程

1. **用户点击"扫码添加设备"**
2. **扫描二维码**（内容可以是 `192.168.0.1` 或 `192.168.0.1:3000`）
3. **格式验证**：
   - ✅ 如果是纯 IP：添加设备，使用当前设置的 RTP 端口
   - ✅ 如果是 IP:Port：添加设备，并更新 RTP 端口为新值
   - ❌ 如果格式错误：显示错误提示
4. **显示结果**：
   - 成功：显示 `已添加设备：192.168.0.1:3000`
   - 失败：显示错误信息，包含格式示例

### 格式验证规则

#### 纯 IP 格式验证
- 格式：`192.168.0.1`
- 每个段：0-255
- 4个段，用 `.` 分隔

#### IP:Port 格式验证
- 格式：`192.168.0.1:3000`
- IP 部分遵循纯 IP 规则
- 端口范围：1-65535
- IP 和端口用 `:` 分隔

#### 错误提示信息
当格式验证失败时，显示以下提示：
```
格式不正确。支持的格式：
• 纯 IP：192.168.0.1
• IP:Port：192.168.0.1:3000
端口范围：1-65535
```

## 显示逻辑

### 设备菜单（DeviceMenu）
- 每个设备显示为：`192.168.0.1:3000`
- 统一使用当前的 RTP 端口

### 状态指示器（StatusIndicator）
- 显示已连接设备列表
- 每个设备显示为：`• 192.168.0.1:3000`
- 最多显示 3 个设备
- 超过 3 个显示：`+ N 更多`

## 测试

### 单元测试文件
**位置**：`app/src/test/java/com/dragon/videorecorder/utils/IpPortValidatorTest.kt`

**测试覆盖**：
- ✅ 有效 IP 地址验证
- ✅ 无效 IP 地址验证
- ✅ 有效 IP:Port 格式验证
- ✅ 无效 IP:Port 格式验证
- ✅ 解析纯 IP
- ✅ 解析 IP:Port
- ✅ 无效输入解析
- ✅ 完整验证流程测试

## 示例场景

### 场景 1：扫描纯 IP
```
输入：192.168.0.1
验证：✅ 通过
结果：
  - 添加设备：192.168.0.1
  - 使用当前 RTP 端口：40018
  - 显示：192.168.0.1:40018
```

### 场景 2：扫描 IP:Port
```
输入：192.168.0.1:3000
验证：✅ 通过
结果：
  - 添加设备：192.168.0.1
  - 更新 RTP 端口：3000
  - 显示：192.168.0.1:3000
```

### 场景 3：扫描无效格式
```
输入：192.168.0
验证：❌ 失败
结果：
  - 显示错误提示
  - 不添加设备
  - 提示支持的格式示例
```

## 注意事项

1. **端口验证**：
   - 端口必须在 1-65535 范围内
   - 如果提供的端口超出范围，将拒绝添加设备

2. **端口配置**：
   - 扫码 IP:Port 时会自动更新 RTP 端口
   - 端口更新会持久化保存
   - 如果端口有警告信息（如常见端口），会显示警告

3. **设备去重**：
   - 纯 IP 作为唯一标识
   - 相同 IP 的设备只能添加一次
   - 重复添加会提示"该设备已存在"

4. **显示统一性**：
   - 所有设备显示都使用统一的 IP:Port 格式
   - 端口信息来自全局 RTP 端口设置
   - 即使扫码时只提供 IP，也会显示为 IP:Port 格式
