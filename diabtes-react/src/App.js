import React, { useState } from 'react';
import axios from 'axios';
import './App.css'
function App() {
  const [formData, setFormData] = useState({
    age: '',
    bmi: '',
    glucose: '',
    bloodPressure: '',
    insulin: '',
    skinThickness: '',
    pregnancies: ''
  });

  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setPrediction(null);

    try {
      const response = await axios.post(`${process.env.REACT_APP_API_URL}/predict`, formData);
      setPrediction(response.data.prediction);
    } catch (error) {
      console.error('Prediction error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-container">
      <h2>Diabetes Risk Prediction</h2>
      <form onSubmit={handleSubmit}>
        {Object.keys(formData).map((field) => (
          <div key={field} style={{ marginBottom: 20 }}>
            <label>{field.charAt(0).toUpperCase() + field.slice(1)}:</label>
            <input
              type="number"
              name={field}
              value={formData[field]}
              onChange={handleChange}
              required
            />
          </div>
        ))}
        <button type="submit">
          {loading ? 'Predicting...' : 'Predict'}
        </button>
      </form>

      {prediction && (
        <div className="prediction-result">
          Result: {prediction}
        </div>
      )}
    </div>
  );
}

export default App;
