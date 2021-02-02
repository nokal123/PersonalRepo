#created by Noah Kalandar for Machine Learning at Yeshiva University 

# recursive version 
def recur_fibo(n):
   if n <= 1:
       return n
   else:
       return (recur_fibo(n-1) + recur_fibo(n-2))

# imperitive version 
function fibonacci(n) {  
  var a = 0, b = 1, sum = 0;
  while (n>1) {
    sum = a + b;
    a = b;
    b = sum;
    n = n - 1;
  }
  return sum;
}

#matrix version 
def matrix_fibonacci(target: int) -> int:
    # initialise a matrix [[1,1],[1,0]] of the first two numbers in the sequence
    v1, v2, v3 = (1, 1, 0, )  

    for record in bin(target)[3:]:  # perform fast exponentiation of the matrix (quickly raise it to the nth power)
        calc = v2 * v2
        v1, v2 = v1 * v1 + calc, (v1 + v3) * v2
        v3 = calc + v3 * v3

        if record is "1":
            v1, v2, v3 = v1 + v2, v1, v2
    return int(v2)