import java.math.BigInteger;
import java.util.Random;
import java.util.Vector;

public class DiffieHellman{
	private class PrimeNumberGen {
		private long n;
		public long getPrimeNumber(){
			this.n = (int)(new Random().nextDouble()*100)+250;
			long l = 0;
			l = (long) ((this.n)*(Math.log(this.n) + (Math.log(Math.log(this.n)) -1) + ((Math.log(Math.log(this.n))-2)/(Math.log(this.n))) - ((Math.log(Math.log(this.n)) -21.0/10.0)/Math.log(this.n)) ));
			for(long i=l;;i++){
				if(isPrime(i)){
					return i;
				}
			}
		}/////////////////////////////////////////////////
		private boolean isPrime(long n){
			if(n%2 == 0 || n%3 == 0) return false;
			for(int i=5; i*i<=n; i+=6){
				if(n%i == 0 || n%(i+2)==0) return false;
			}
			return true;
		}
	}

	public class PrimitiveRootGen {
		long pr, clePrimaire, phi;
		public PrimitiveRootGen(long clePrimaire){
			this.clePrimaire = clePrimaire;
			this.phi = this.clePrimaire - 1;
			Vector<Long> primitiveRoots =  this.getPrimitiveRoot(this.clePrimaire, this.phi);
			this.pr = primitiveRoots.get(new Random().nextInt(primitiveRoots.size()));
		}

		public long getPr() {
			return pr;
		}

		private Vector<Long> getPrimitiveRoot(long clePrimaire, long phi){
			Vector<Long> primeFactors = this.genPrimesFactorsList(phi);
			Vector<Long> primitiveRoots = new Vector<>();
			for(long i = 2;i<clePrimaire;i++){
				boolean flg = false;
				for(Long l: primeFactors){
					BigInteger iBig = BigInteger.valueOf(i);
					BigInteger phiBig = BigInteger.valueOf(phi/l);
					BigInteger pBig = BigInteger.valueOf(clePrimaire);
					BigInteger pRootBig = iBig.modPow(phiBig, pBig);
					if(pRootBig.compareTo(BigInteger.valueOf(1))==0){
						flg = true;
						break;
					}
				}
				if(!flg)primitiveRoots.add(i);
			}
			return primitiveRoots;
		}

		private Vector<Long> genPrimesFactorsList(long phi){
			Vector<Long> primesFactors = new Vector<>();
			while(phi % 2 == 0){
				primesFactors.add((long) 2);
				phi /= 2;
			}
			for(long i=3;i<=Math.sqrt(phi);i+=2){
				if(phi % i == 0){
					primesFactors.add(i);
					phi /= i;
				}
			}
			if(phi > 2){
				primesFactors.add(phi);
			}
			return primesFactors;
		}
	}
	
		BigInteger clePrimaire, clePrimaireRacine;
		BigInteger cleSecrete;
		BigInteger cleFinale;

		public void genClePrimaireEtRacine(){
			this.clePrimaire = BigInteger.valueOf(new PrimeNumberGen().getPrimeNumber());
			this.clePrimaireRacine = BigInteger.valueOf(new PrimitiveRootGen(this.clePrimaire.intValue()).getPr());
		}

		public void genCleSecrete(){
			this.cleSecrete = BigInteger.valueOf(new PrimeNumberGen().getPrimeNumber());
		}

		public BigInteger getClePrimaire() {
			return clePrimaire;
		}

		public void setClePrimaireRacine(BigInteger a) {
			this.clePrimaireRacine = a;
		}

		public void setClePrimaire(BigInteger a) {
			this.clePrimaire = a;
		}

		public BigInteger getClePrimaireRacine() {
			return clePrimaireRacine;
		}

		public BigInteger toServeur(BigInteger cleSecreteClient){
			return this.clePrimaireRacine.modPow(cleSecreteClient, this.clePrimaire);
		}

		public BigInteger toClient(BigInteger cleSecreteServeur){
			return this.clePrimaireRacine.modPow(cleSecreteServeur, this.clePrimaire);
		}

		public void aliceCalculationOfKey (BigInteger toClient, BigInteger cleSecreteClient){
			cleFinale =  toClient.modPow(cleSecreteClient, this.clePrimaire);
		}

		public void bobCalculationOfKey(BigInteger toServeur, BigInteger cleSecreteServeur){
			cleFinale =  toServeur.modPow(cleSecreteServeur, this.clePrimaire);
		}
	}