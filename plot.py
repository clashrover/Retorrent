import numpy as np

import matplotlib.pyplot as plt

def main():
    
    for i in range(50):
        s = str(i)+".txt"
        with open(s) as f:
            x = np.loadtxt(f)
            
            if x.ndim== 1:
                plt.plot(x[0],x[1])
            else:
                m,n = np.shape(x)
                plt.plot(x[0:,0],x[0:,1])

    plt.title('Cumulative Download vs Time')
    plt.xlabel('Time(s)', color='#1C2833')
    plt.ylabel('Download(bytes)', color='#1C2833')
    plt.show()
            


main()
