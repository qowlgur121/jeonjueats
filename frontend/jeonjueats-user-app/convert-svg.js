const fs = require('fs');
const { createCanvas, loadImage } = require('canvas');

async function convertSvgToPng(svgPath, pngPath, width, height) {
    try {
        // SVG 파일 읽기
        const svgContent = fs.readFileSync(svgPath, 'utf8');
        
        // SVG를 Data URL로 변환
        const svgDataUrl = `data:image/svg+xml;base64,${Buffer.from(svgContent).toString('base64')}`;
        
        // 캔버스 생성
        const canvas = createCanvas(width, height);
        const ctx = canvas.getContext('2d');
        
        // 이미지 로드 및 그리기
        const img = await loadImage(svgDataUrl);
        ctx.drawImage(img, 0, 0, width, height);
        
        // PNG로 저장
        const pngBuffer = canvas.toBuffer('image/png');
        fs.writeFileSync(pngPath, pngBuffer);
        
        console.log(`✅ ${pngPath} 생성 완료 (${width}x${height})`);
        return true;
    } catch (error) {
        console.error(`❌ ${pngPath} 생성 실패:`, error.message);
        return false;
    }
}

async function convertAllIcons() {
    console.log('🎨 JeonjuEats 아이콘 변환 시작...');
    
    const conversions = [
        ['public/icon-512x512.svg', 'public/pwa-512x512.png', 512, 512],
        ['public/icon-192x192.svg', 'public/pwa-192x192.png', 192, 192],
        ['public/favicon.svg', 'public/favicon-32x32.png', 32, 32],
    ];
    
    let successCount = 0;
    
    for (const [svgPath, pngPath, width, height] of conversions) {
        const success = await convertSvgToPng(svgPath, pngPath, width, height);
        if (success) successCount++;
    }
    
    console.log(`\n🎉 완료: ${successCount}/${conversions.length}개 아이콘 변환 성공`);
    
    if (successCount === conversions.length) {
        console.log('\n📁 생성된 파일들:');
        console.log('- public/pwa-512x512.png');
        console.log('- public/pwa-192x192.png'); 
        console.log('- public/favicon-32x32.png');
    }
}

// 실행
convertAllIcons().catch(console.error);