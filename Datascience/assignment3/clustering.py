import sys
import numpy as np

#입력값 검사 및 입력 받기
if len(sys.argv) != 5:
    print("check your arguments")
    sys.exit()

input_file_name = sys.argv[1] # input파일 이름
try: # int 형으로 받기
    n = int(sys.argv[2])        # cluster 개수
    Eps = int(sys.argv[3])      # 이웃할 수 있는 최대 반지름
    MinPts = int(sys.argv[4])   # 최대 반지름으로 이웃하다고 할 수 있는
                                # 최소 points의 개수
except ValueError:  #int로 변환 불가능한 경우
    print("check your type of arguments")
    sys.exit()


# 데이터 저장할 구조
class Point:
    objID = 0
    x = .0
    y = .0

    def __init__(self,objID,x,y):
        self.objID = int(objID)
        self.x = x
        self.y = y

        
# input file 읽어서 Points에 저장
try:
    with open(input_file_name) as input_file:
        points = []
        for line in input_file:
            objID,x,y = map(float, line.split('\t'))
            points.append(Point(objID,x,y))

except FileNotFoundError:   # input file있는지 체크
    print("check your input data file")
    sys.exit()


# 데이터를 numpy array로 저장
points = np.array([[point.x, point.y] for point in points])


# 거리 구하기(유클리디안)
def euclidean_distance(a, b):
    return np.linalg.norm(a - b)


# 이웃 찾기
def find_neighbors(points, point_index, Eps):
    distances = np.linalg.norm(points - points[point_index], axis=1)
    neighbors = np.where(distances <= Eps)[0]
    return neighbors.tolist()


# 클러스터 확장
def expand_cluster(points, point_index, neighbors, cluster, Eps, MinPts):
    cluster.append(point_index)
    i = 0
    while i < len(neighbors):
        neighbor = neighbors[i]
        if not visited[neighbor]:
            visited[neighbor] = True
            neighbor_neighbors = find_neighbors(points, neighbor, Eps)
            if len(neighbor_neighbors) >= MinPts:
                neighbors.extend(neighbor_neighbors)
        if neighbor not in cluster:
            cluster.append(neighbor)
        i += 1


# dbscan 알고리즘 구현
def dbscan(points, Eps, MinPts):
    clusters = []
    cluster_label = 0

    for i in range(len(points)):
        if visited[i]:
            continue
        visited[i] = True
        neighbors = find_neighbors(points, i, Eps)
        if len(neighbors) < MinPts:
            continue
        cluster = []
        expand_cluster(points, i, neighbors, cluster, Eps, MinPts)
        clusters.append(cluster)
        cluster_label += 1

    return clusters


# 데이터 방문 기록
visited = np.zeros(len(points), dtype=bool)

# 결과값 저장
clusters = dbscan(points, Eps, MinPts)

# 클러스터 개수가 n이 넘을 때
while len(clusters) > n:
    minIdx = 0
    Min = len(clusters[0])
    for i in range(len(clusters)):
        length = len(clusters[i])
        if length < Min:
            minIdx = i
            Min = length
    clusters.pop(minIdx)


# 출력 형식 맞춰서 출력해주기
idx = 0
while clusters:
    cluster = clusters.pop(0)
    output_file_name = input_file_name[:-4] + "_cluster_"+str(idx)+".txt" 
    idx+=1
    with open(output_file_name,"w") as output_file:
        while cluster:
            result = str(cluster.pop(0)) + "\n"
            output_file.write(result)