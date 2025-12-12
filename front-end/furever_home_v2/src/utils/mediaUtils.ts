/**
 * 媒体文件工具函数
 */

/**
 * 判断URL是否为视频文件
 * @param url 文件URL
 * @returns true表示是视频，false表示是图片或其他
 */
export function isVideoUrl(url: string): boolean {
  if (!url || typeof url !== 'string') {
    return false;
  }
  
  const urlLower = url.toLowerCase();
  const videoExtensions = ['.mp4', '.mov', '.avi', '.wmv', '.flv', '.webm', '.mkv', '.m4v', 'mp4', 'mov', 'avi', 'wmv', 'flv', 'webm', 'mkv', 'm4v'];
  
  // 检查URL路径中是否包含视频扩展名
  try {
    const urlObj = new URL(urlLower.startsWith('http') ? urlLower : `http://example.com${urlLower}`);
    const pathname = urlObj.pathname;
    
    // 检查路径中的扩展名
    if (videoExtensions.some(ext => pathname.includes(`.${ext}`) || pathname.endsWith(ext))) {
      return true;
    }
    
    // 检查完整的URL字符串中是否包含视频扩展名
    if (videoExtensions.some(ext => urlLower.includes(`.${ext}`) || urlLower.endsWith(ext))) {
      return true;
    }
    
    // 检查URL中是否包含视频相关的关键词（用于后端可能没有扩展名的情况）
    const videoKeywords = ['/video/', '/videos/', 'video', 'mp4', 'mov'];
    if (videoKeywords.some(keyword => urlLower.includes(keyword))) {
      // 排除图片相关关键词
      const imageKeywords = ['/image/', '/images/', 'image', 'jpg', 'jpeg', 'png', 'gif'];
      if (!imageKeywords.some(keyword => urlLower.includes(keyword))) {
        return true;
      }
    }
  } catch (e) {
    // 如果URL解析失败，使用简单的字符串匹配
    if (videoExtensions.some(ext => urlLower.includes(`.${ext}`) || urlLower.includes(ext))) {
      return true;
    }
  }
  
  return false;
}

/**
 * 判断URL是否为图片文件
 * @param url 文件URL
 * @returns true表示是图片，false表示是视频或其他
 */
export function isImageUrl(url: string): boolean {
  if (!url || typeof url !== 'string') {
    return false;
  }
  
  // 提取URL的文件扩展名
  const urlLower = url.toLowerCase();
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg'];
  
  // 检查URL是否包含图片扩展名
  return imageExtensions.some(ext => urlLower.includes(ext) || urlLower.endsWith(ext));
}

/**
 * 获取媒体类型
 * @param url 文件URL
 * @returns 'video' | 'image' | 'unknown'
 */
export function getMediaType(url: string): 'video' | 'image' | 'unknown' {
  if (isVideoUrl(url)) {
    return 'video';
  }
  if (isImageUrl(url)) {
    return 'image';
  }
  return 'unknown';
}
