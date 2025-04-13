from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler
import numpy as np
import pickle
import bz2
import os

# Sample data for fitting the PCA model (replace with your actual dataset)
data = np.random.rand(100, 5)  # Example: 100 samples with 5 features each

# Standardizing the data
scaler = StandardScaler()
scaled_data = scaler.fit_transform(data)

# Applying PCA
pca = PCA(n_components=2)  # Example: reducing to 2 components
pca.fit(scaled_data)

# Ensure the Model directory exists
os.makedirs("Model", exist_ok=True)

# Save the PCA model as a compressed .pkl file
with bz2.BZ2File("Model/pcaModel.pkl", "wb") as f:
    pickle.dump(pca, f)

print("PCA model saved as 'pcaModel.pkl'.")
