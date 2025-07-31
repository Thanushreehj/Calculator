public class CalculationResult {
    private double num1;
    private double num2;
    private String operation;
    private Double result;
    private String error;

    public CalculationResult() {}

    public String toJson() {
        if (error != null) {
            return "{\"error\": \"" + error + "\"}";
        }
        return "{\"num1\": " + num1 + ", "
                + "\"num2\": " + num2 + ", "
                + "\"operation\": \"" + operation + "\", "
                + "\"result\": " + result + "}";
    }

    // Getters and Setters
    public double getNum1() { return num1; }
    public void setNum1(double num1) { this.num1 = num1; }

    public double getNum2() { return num2; }
    public void setNum2(double num2) { this.num2 = num2; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public Double getResult() { return result; }
    public void setResult(Double result) { this.result = result; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
