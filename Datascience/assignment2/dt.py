import sys
import pandas as pd
import math

# 입력값 검사 및 입력 받기
if len(sys.argv) != 4:
    print("check your arguments")
    sys.exit()
training_file_name = sys.argv[1]    # 훈련 데이터셋
test_file_name = sys.argv[2]        # 테스트 데이터셋
output_file_name = sys.argv[3]      # 출력파일

# 훈련파일에서 Label 분리
def preprocess_training_data(file):
    data = pd.read_csv(file, sep='\t')

    columns = data.columns
    n = len(columns)

    X = data.drop(columns[n-1], axis=1)
    y = data[columns[n-1]]
    label = columns[n-1]
    return X, y, label;

class DecisionTree:
    # 초기화
    def __init__(self):
        self.tree = None
        self.default_class = None
    
    # 트리 만들기
    def fit(self, X, y):
        self.tree = self._build_tree(X, y, list(range(len(X.columns))),[])
        self.default_class = self._majority_vote(y)                         # path에 없는 것 다수결로 결정
    
    # 트리 재귀적으로 구현
    def _build_tree(self, X, y, attributes, used_attributes):
        # 종료조건
        if X.empty or y.empty:
            return None
        if len(set(y)) == 1:
            return y.iloc[0]
        if not attributes:
            return self._majority_vote(y)
        
        # 가장 좋은 attribute 선택하기
        best_attribute = self._select_attribute(X, y, used_attributes)
        if best_attribute is None:
            return self._majority_vote(y)
        
        # 트리 붙이기
        tree = {best_attribute: {}}
        attribute_values = X[best_attribute].unique()   # categorical 데이터이기 때문에 values 찾아주기
        used_attributes.append(best_attribute)          # 하나의 path에 같은 attribute로 분류할 수 없도록 사용 attribute 저장
        

        for value in attribute_values:
            sub_X, sub_y = self._subset(X, y, best_attribute, value)
            sub_attributes = [a for a in attributes if a not in used_attributes]
            
            if not sub_attributes:
                leaf_node = self._majority_vote(sub_y)
                tree[best_attribute][value] = leaf_node
            else:
                subtree = self._build_tree(sub_X, sub_y, sub_attributes, used_attributes.copy())
                tree[best_attribute][value] = subtree
                
        used_attributes.remove(best_attribute)
        return tree
    
    # 서브셋 만들기
    def _subset(self, X, y, attribute, value):
        mask = X[attribute] == value
        return X[mask], y[mask]
    
    # 다수결 적용
    def _majority_vote(self, y):
        counts = pd.Series(y).value_counts()
        return counts.index[0]
    
    # attribute 선택하기 - C4.5 사용
    def _select_attribute(self, X, y, used_attributes):
        # 엔트로피 계산
        entropy = self._entropy(y)
        
        gains = {}
        splits = {}
        for attr in X.columns:
            if attr in used_attributes:                             # 한 path당 한번 사용
                continue
            gain, split = self._gain_ratio(X, y, attr, entropy)
            gains[attr] = gain
            splits[attr] = split
        
        # gain ratio가 가장 큰것을 사용
        best_attr = max(gains, key=gains.get)
        used_attributes.append(best_attr)
        return best_attr

    # 엔트로피 구하기
    def _entropy(self, y):
        counts = pd.Series(y).value_counts()
        probabilities = counts / counts.sum()
        return -1 * sum([p * math.log2(p) for p in probabilities])

    # gain ratio 구하기
    def _gain_ratio(self, X, y, attr, parent_entropy):
        attr_counts = X[attr].value_counts()
        attr_probabilities = attr_counts / attr_counts.sum()
        attr_entropy = sum([attr_probabilities[val] * self._entropy(y[X[attr] == val])
                            for val in attr_counts.index])
        
        gain = parent_entropy - attr_entropy
        
        # split 계산
        splits = -1 * sum([p * math.log2(p) for p in attr_probabilities])
        
        # 0으로 나누는 것 피하기
        if splits == 0:
            return float('-inf'), splits
        
        gain_ratio = gain / splits
        
        return gain_ratio, splits
    
    # 예측
    def predict(self, X_test):
        y_pred = []
        for _, row in X_test.iterrows():
            y_pred.append(self._predict_single(row, self.tree))
        return y_pred
    def _predict_single(self, row, tree):
        if isinstance(tree, str):
            return tree
        else:
            attribute = list(tree.keys())[0]
            value = row.get(attribute, None)
            if value is None:
                return self.default_class  # 경로에 없는 것
            else:
                try:
                    subtree = tree[attribute][value]
                except KeyError:
                    return self.default_class
                return self._predict_single(row, subtree)
        

X_train, y_train, label = preprocess_training_data(training_file_name)
dt = DecisionTree()                                                     # 트리 만들기
dt.fit(X_train, y_train)                                                # 훈련셋 학습
X_test = pd.read_csv(test_file_name, sep='\t')                          # 테스트셋 읽기
y_pred = dt.predict(X_test)                                             # 훈련기반으로 테스트셋 예측
X_test[label]= y_pred                                                   # 예측한 것 붙이기

# 출력파일 생성
with open(output_file_name,'w') as output:
    temp = ""
    for i in range(len(list(X_test.columns))):
        temp+=list(X_test.columns)[i]
        if i < len(list(X_test.columns))-1:
            temp+='\t'
        else:
            temp += '\n'
    output.write(temp)
    rows = X_test.values.tolist()
    for row in range(len(rows)):
        temp = ""
        for i in range(len(rows[row])):
            temp+=rows[row][i]
            if i < len(rows[row])-1:
                temp+='\t'
            else:
                temp += '\n'
        output.write(temp)
