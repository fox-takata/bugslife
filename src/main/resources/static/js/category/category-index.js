// $(document).load(function () {
//   var tooltipTriggerList = [].slice.call(
//     document.querySelectorAll('[data-bs-toggle="tooltip"]')
//   );
//   var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
//     return new bootstrap.Tooltip(tooltipTriggerEl, {
//       trigger: "click",
//     });
//   });
// });

document.addEventListener('DOMContentLoaded', function () {
  var clickableIcons = document.querySelectorAll('.clickable-tooltip-icon');
  var tooltipInstance = null; // ツールチップのインスタンスを保持する変数

  // ドキュメント全体のクリックイベントを監視
  document.addEventListener('click', function (event) {
    // クリックされた要素がボタンでない場合はツールチップを閉じる
    if (!event.target.classList.contains('clickable-tooltip-icon') && tooltipInstance) {
      tooltipInstance.dispose();
      tooltipInstance = null;
    }
  });

  clickableIcons.forEach(function (icon) {
    icon.addEventListener('click', function () {
      if (!tooltipInstance) {
        // ツールチップが存在しない場合は新しいツールチップを作成
        var categoryId = icon.getAttribute('data-category-id');
        var xhr = new XMLHttpRequest();
        xhr.open('GET', '/api/categories/' + categoryId + '/categoryNames', true);
        xhr.onreadystatechange = function () {
          if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
              var categoryNames = JSON.parse(xhr.responseText);
              var tooltipContent = '現在紐づいている商品一覧';
              tooltipContent += '<ul>';
              if (categoryNames.length == 0) {
                tooltipContent += 'ありません';
              }
              for (var i = 0; i < categoryNames.length; i++) {
                tooltipContent += '<li>' + categoryNames[i] + '</li>';
              }
              tooltipContent += '</ul>';

              tooltipInstance = new bootstrap.Tooltip(icon, {
                title: tooltipContent,
                html: true,
                placement: 'top',
                trigger: 'manual'
              });

              tooltipInstance.show(); // ツールチップを表示
            } else {
              console.error('Error: ' + xhr.status);
            }
          }
        };
        xhr.send();
      } else {
        // ツールチップが既に存在する場合は閉じる
        tooltipInstance.dispose();
        tooltipInstance = null; // ツールチップのインスタンスをクリア
      }
    });
  });
});
