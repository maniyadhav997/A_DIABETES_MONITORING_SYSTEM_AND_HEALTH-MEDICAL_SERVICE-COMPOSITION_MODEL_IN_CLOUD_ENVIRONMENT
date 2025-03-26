import pandas as pd
import numpy as np
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, confusion_matrix, roc_curve, roc_auc_score
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.utils.extmath import safe_sparse_dot

# Read dataset
data = pd.read_csv("Diabetes-Prediction-With-deployment/Dataset/diabetes.csv")

# Replace 0s with mean in relevant columns
data['BMI'].replace(0, data['BMI'].mean(), inplace=True)
data['BloodPressure'].replace(0, data['BloodPressure'].mean(), inplace=True)
data['Glucose'].replace(0, data['Glucose'].mean(), inplace=True)
data['Insulin'].replace(0, data['Insulin'].mean(), inplace=True)
data['SkinThickness'].replace(0, data['SkinThickness'].mean(), inplace=True)

# Separate features and target variable
X = data.drop(columns=['Outcome'])
y = data['Outcome']

# Standardize the dataset
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Apply PCA to reduce dimensions
pca = PCA(n_components=5)  # Reduce to 5 components
X_pca = pca.fit_transform(X_scaled)

# Split dataset
X_train, X_test, y_train, y_test = train_test_split(X_pca, y, test_size=0.25, random_state=0)

# Define Extreme Learning Machine (ELM) Model
class ELM:
    def __init__(self, n_hidden_neurons=50, activation_function=np.tanh):
        self.n_hidden_neurons = n_hidden_neurons
        self.activation_function = activation_function
    
    def fit(self, X, y):
        np.random.seed(0)
        self.input_weights = np.random.randn(X.shape[1], self.n_hidden_neurons)
        self.biases = np.random.randn(self.n_hidden_neurons)
        H = self.activation_function(safe_sparse_dot(X, self.input_weights) + self.biases)
        self.output_weights = np.linalg.pinv(H) @ y
    
    def predict(self, X):
        H = self.activation_function(safe_sparse_dot(X, self.input_weights) + self.biases)
        return (H @ self.output_weights > 0.5).astype(int)

# Train ELM model
elm = ELM(n_hidden_neurons=100)
elm.fit(X_train, y_train)
y_pred = elm.predict(X_test)

# Evaluate the model
accuracy = accuracy_score(y_test, y_pred)
conf_mat = confusion_matrix(y_test, y_pred)
auc = roc_auc_score(y_test, y_pred)

print(f"Accuracy: {accuracy}")
print(f"Confusion Matrix:\n{conf_mat}")
print(f"AUC Score: {auc}")

# Plot ROC curve
fpr, tpr, _ = roc_curve(y_test, y_pred)
plt.plot(fpr, tpr, color='orange', label='ROC Curve')
plt.plot([0, 1], [0, 1], color='darkblue', linestyle='--', label=f'AUC = {auc:.2f}')
plt.xlabel('False Positive Rate')
plt.ylabel('True Positive Rate')
plt.title('Receiver Operating Characteristic (ROC) Curve')
plt.legend()
plt.show()
