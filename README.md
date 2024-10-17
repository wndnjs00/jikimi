# ⚡️지키미⚡️
지진발생시 주변 대피소를 한눈에 확인할 수 있는 안전지킴이앱!
<br/><br/>
<img width="870" alt="image" src="https://github.com/user-attachments/assets/0088a265-ca97-46cd-8741-91be123088ac">
<br/><br/>
## 🕖 개발 기간 [구현중]
2024.08.16 ~ 
<br/><br/>
## 프로젝트 소개
갑작스런 지진 발생시에도, 내주변의 대피장소를 빠르게 확인하고 대피하여 인명피해를 최소화하기위한 목적으로 개발했습니다.
<br/>`지키미`를 통해 지진에 안전하게 대비하고, 재난발생시 행동요령까지 배워보세요!
<br/><br/>
## ⚒️기술스택
|분류|이름|
|:---:|:---:|
|언어|<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=Kotlin&logoColor=white">
|개발 환경|<img src="https://img.shields.io/badge/android studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white">
|Architecture|<img src="https://img.shields.io/badge/mvvm-221E68?style=for-the-badge&logoColor=white"><img src="https://img.shields.io/badge/repository pattern-221E68?style=for-the-badge&logoColor=white">
|DI|<img src="https://img.shields.io/badge/hilt-221E68?style=for-the-badge&logoColor=white">
|비동기 처리|<img src="https://img.shields.io/badge/flow-221E68?style=for-the-badge&logoColor=white"><img src="https://img.shields.io/badge/coroutine-221E68?style=for-the-badge&logoColor=white">
|Networking Tool|<img src="https://img.shields.io/badge/retrofit-221E68?style=for-the-badge&logoColor=white"><img src="https://img.shields.io/badge/okhttp-221E68?style=for-the-badge&logoColor=white">
|이미지 로더|<img src="https://img.shields.io/badge/coil-221E68?style=for-the-badge&logoColor=white">
|지도|<img src="https://img.shields.io/badge/naver map-3DDC84?style=for-the-badge&logo=naver&logoColor=white">
|Database|<img src="https://img.shields.io/badge/room-221E68?style=for-the-badge&logoColor=white">
|라이브러리|<img src="https://img.shields.io/badge/paging3[적용예정]-221E68?style=for-the-badge&logoColor=white"><img src="https://img.shields.io/badge/음성인식 라이브러리[적용예정]-221E68?style=for-the-badge&logoColor=white">
|UI|<img src="https://img.shields.io/badge/xml-221E68?style=for-the-badge&logoColor=white"><img src="https://img.shields.io/badge/navigation-221E68?style=for-the-badge&logoColor=white">
|협업|<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white"><img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">


<br/><br/>
## 💻 주요기능


### 대피 페이지

- **현재위치 반경 5km이내**의 야외대피장소와 임시주거시설의 위치표시
- 텍스트 또는 음성인식을 통하여 **야외대피장소와 임시주거시설의 위치 검색**
- 마커 클릭시, **야외대피장소와 임시주거시설의 상세정보 제공**
- **좋아요**를 통한 야외대피장소,임시주거시설 저장

### 재난상식 페이지

- 다양한 **재난행동요령** 제공




<br/><br/>
## 🚨트러블슈팅

1) Manifest내에 작성되는 키값을 local.properties로 빼주는 작업시 발생하는 문제
    - 네이버맵 sdk를 사용해서 맵을 띄울때 client ID는 **Manifest내에 작성되는 키**이기 때문에, **buildConfigField() 대신 addManifestPlaceholders()함수를 사용**해서 해결
<br/>
 
2) API 연속호출로 인한 비효율적인 문제
    - 모든 지역의 위경도좌표를 전부 호출하면 **과도한 호출로인한 데이터낭비** 발생
    - **circleOverlay() 반경을 5km로 지정**하고 **해당반경내에 있는 데이터만 마커로 표시해주는 로직을 적용**하여, **사용자경험을 향상**시키고 **시스템성능을 효율적으로 관리**하도록 해결
<br/>

3) Room을 통해 데이터를 삭제하는 로직이 올바르게 작동하지 않는 문제
    - **vtAcmdfcltyNm[대피소명]값을 기준**으로 **해당값과 일치할때 삭제하는 로직을 추가**해, 더욱 정확하게 데이터를 삭제하도록하여 해결
 


<br/><br/>
### ⚙️기술적 의사결정

- **Single Activity Architecture(SAA) 적용이유**

  - Acticity는 Fragment에 비해 상대적으로 무겁기때문에, **메모리 효율성을 높이기 위함**
  - **Fragment를 사용하여 복잡한 UI에 쉽게 대처**하기 위함
