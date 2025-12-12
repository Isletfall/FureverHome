<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-section">
        <h3>数据统计</h3>
        <p>我们与多家救助站建立了长期合作关系</p>
        <p>已为 <strong>1,286</strong> 只小动物找到新家</p>
        <a href="#" style="color: #ff8c42">查看合作救助站 →</a>
      </div>
      <div class="footer-section">
        <h3>联系开发团队</h3>
        <p>如果您有任何问题、建议或合作意向，请随时联系我们的开发团队。</p>
        <div class="social-icons">
          <img src="https://img.icons8.com/color/24/wechat.png" alt="微信" />
          <img src="https://img.icons8.com/color/24/qq.png" alt="QQ" />
          <img src="https://img.icons8.com/color/24/weibo.png" alt="微博" />
        </div>
      </div>
    </div>
    <div class="copyright">2025 FUREVERHOME动物领养平台 - 让每个生命都有温暖的家</div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: #1e293b;
  color: #fff;
  padding: 40px 5% 20px;
  margin-top: 60px;
}

.footer-content {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  gap: 30px;
}

.footer-section {
  flex: 1;
  min-width: 220px;
}

.footer-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
}

.footer-section p {
  margin-bottom: 8px;
  font-size: 14px;
  color: #ccc;
}

.social-icons {
  margin-top: 10px;
  display: flex;
  gap: 12px;
}

.social-icons img {
  width: 24px;
  height: 24px;
  cursor: pointer;
  transition: transform 0.3s;
}

.social-icons img:hover {
  transform: scale(1.15);
}

.copyright {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 14px;
  color: #aaa;
}
</style>
