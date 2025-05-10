# CurveFitter - 交互式曲线拟合编辑器
基于 Java Swing 实现的交互式曲线编辑器，支持 ​**NURBS**​ 和 ​**贝塞尔曲线**​ 两种算法，提供控制点权重调整、斜率手柄等高级功能。
##  功能特性
- ​**两种曲线算法**​
  - ​**NURBS**​ (非均匀有理B样条)
  - ​**贝塞尔曲线**​
- ​**交互式编辑**​
  - 左键单击添加控制点
  - 右键单击删除控制点
  - delete删除所有控制点
  - 拖动控制点调整位置
  - 通过斜率手柄调整曲线形状
  - 键盘↑/↓实时调整权重
- ​**可视化辅助**​
  - 实时曲线渲染
  - 控制点编号标识
  - 权重和坐标显示

## 技术架构
### 代码结构
```text
src/
├── main/
│   ├── java/
│   │   ├── algorithm/       # 曲线算法核心
│   │   │   ├── CurveAlgorithm.java  # 算法接口
│   │   │   ├── EnhancedBezier.java   # 贝塞尔实现
│   │   │   └── EnhancedNURBS.java    # NURBS实现
│   │   ├── model/           # 数据模型
│   │   │   ├── ControlPoint.java    # 控制点模型
│   │   │   └── SlopeHandle.java     # 斜率手柄模型
│   │   ├── ui/              # 用户界面
│   │   │   ├── CurveFitter.java     # 程序入口
│   │   │   ├── DrawingPanel.java    # 绘图面板
│   │   │   └── MainPanel.java       # 主界面
│   │   └── Main.java        # 启动类
