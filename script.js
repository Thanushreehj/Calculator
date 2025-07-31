class Calculator {
  constructor() {
    this.apiUrl = '/api/calculate';
    this.initializeElements();
    this.attachEventListeners();
  }

  initializeElements() {
    this.num1Input = document.getElementById('num1');
    this.num2Input = document.getElementById('num2');
    this.operationButtons = document.querySelectorAll('.operation-btn');
    this.resultDisplay = document.getElementById('result-display');
    this.clearButton = document.getElementById('clear-btn');
    this.loadingOverlay = document.getElementById('loading');
  }

  attachEventListeners() {
    this.operationButtons.forEach(button => {
      button.addEventListener('click', () => {
        const operation = button.getAttribute('data-operation');
        this.performCalculation(operation);
      });
    });

    this.clearButton.addEventListener('click', () => {
      this.clearAll();
    });

    this.num1Input.addEventListener('keypress', e => {
      if (e.key === 'Enter') this.num2Input.focus();
    });

    this.num2Input.addEventListener('keypress', e => {
      if (e.key === 'Enter') this.performCalculation('add');
    });
  }

  async performCalculation(operation) {
    const num1 = parseFloat(this.num1Input.value);
    const num2 = parseFloat(this.num2Input.value);

    if (isNaN(num1) || isNaN(num2)) {
      this.displayError('Please enter valid numbers');
      return;
    }

    this.showLoading();

    try {
      const response = await fetch(this.apiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ num1, num2, operation })
      });

      const result = await response.json();
      this.hideLoading();

      if (result.error) {
        this.displayError(result.error);
      } else {
        this.displayResult(result);
      }

    } catch (error) {
      this.hideLoading();
      this.displayError('Connection error: Server not reachable');
      console.error('API Error:', error);
    }
  }

  displayResult(result) {
    const symbols = { add: '+', subtract: '−', multiply: '×', divide: '÷' };
    const symbol = symbols[result.operation];

    this.resultDisplay.className = 'result-display success';
    this.resultDisplay.innerHTML = `
      <div>
        <div class="calculation">${result.num1} ${symbol} ${result.num2}</div>
        <div class="answer">${result.result}</div>
      </div>
    `;
  }

  displayError(msg) {
    this.resultDisplay.className = 'result-display error';
    this.resultDisplay.innerHTML = `<div class="error-message">${msg}</div>`;
  }

  clearAll() {
    this.num1Input.value = '';
    this.num2Input.value = '';
    this.resultDisplay.className = 'result-display';
    this.resultDisplay.innerHTML = '<p class="placeholder">Select an operation to see result</p>';
    this.num1Input.focus();
  }

  showLoading() {
    this.loadingOverlay.classList.remove('hidden');
  }

  hideLoading() {
    this.loadingOverlay.classList.add('hidden');
  }
}

// Initialize the calculator
window.onload = () => {
  new Calculator();
};
