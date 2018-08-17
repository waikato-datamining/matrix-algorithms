# matrix-algorithms

Java library of 2-dimensional matrix algorithms. 

## Algorithms

Unsupervised:

* [PCA](https://web.archive.org/web/20160630035830/http://statmaster.sdu.dk:80/courses/ST02/module05/module.pdf)

Supervised:

* [PLS1](https://web.archive.org/web/20081001154431/http://statmaster.sdu.dk:80/courses/ST02/module07/module.pdf)
* [SIMPLS](http://www.statsoft.com/textbook/partial-least-squares/#SIMPLS)
* [KernelPLS](http://www.plantbreeding.wzw.tum.de/fileadmin/w00bdb/www/kraemer/icml_kernelpls.pdf)
* [OSC](https://www.r-bloggers.com/evaluation-of-orthogonal-signal-correction-for-pls-modeling-osc-pls-and-opls/)
* [NIPALS](http://www.statsoft.com/textbook/partial-least-squares/#NIPALS)
* [SparsePLS](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2810828/)
* [GLSW](http://wiki.eigenvector.com/index.php?title=Advanced_Preprocessing:_Multivariate_Filtering#GLSW_Algorithm) (orthogonal signal correction)
* [EPO](http://wiki.eigenvector.com/index.php?title=Advanced_Preprocessing:_Multivariate_Filtering#External_Parameter_Orthogonalization_.28EPO.29) (External Parameter Orthogonalization)
* [CCA](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.30.16)

Planned:

* [rPLS](https://www.researchgate.net/publication/259536250_Recursive_weighted_partial_least_squares_rPLS_An_efficient_variable_selection_method_using_PLS)
* [iPLS](https://www.researchgate.net/publication/247776629_Interval_Partial_Least-Squares_Regression_iPLS_A_Comparative_Chemometric_Study_with_an_Example_from_Near-Infrared_Spectroscopy)
* [PLS2](https://web.archive.org/web/20160702070233/http://statmaster.sdu.dk/courses/ST02/module08/module.pdf)
* [mwPLS]()
* [biPLS](https://www.academia.edu/14468430/Sequential_application_of_backward_interval_partial_least_squares_and_genetic_algorithms_for_the_selection_of_relevant_spectral_regions)
* ...
  
## Maven

Add the following dependency to your `pom.xml`:

```xml
    <dependency>
      <groupId>nz.ac.waikato.cms.adams</groupId>
      <artifactId>matrix-algorithms</artifactId>
      <version>0.0.2</version>
    </dependency>
```
## Examples

### PCA

```java
import Jama.Matrix;
import com.github.waikatodatamining.matrix.algorithm.PCA;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
...
Matrix data = MatrixHelper.read("bolts.csv", true, ',');
// remove the class column, if present
//data = MatrixHelper.deleteCol(data, data.getColumnDimension() - 1);

System.out.println("\nInput");
System.out.println(MatrixHelper.toString(data));

PCA pca = new PCA();
Matrix transformed = pca.transform(data);
System.out.println("\nTransformed");
System.out.println(MatrixHelper.toString(transformed));
```

### SIMPLS
```java
import Jama.Matrix;
import com.github.waikatodatamining.matrix.algorithm.pls.SIMPLS;
import com.github.waikatodatamining.matrix.core.MatrixHelper;
...
Matrix predictors = MatrixHelper.read("bolts.csv", true, ',');
Matrix response = MatrixHelper.read("bolts_response.csv", true, ',');

System.out.println("\nPredictors");
System.out.println(MatrixHelper.toString(predictors));

System.out.println("\nResponse");
System.out.println(MatrixHelper.toString(response));

SIMPLS pls = new SIMPLS();
pls.setNumComponents(3);
String msg = pls.initialize(predictors, response);
if (msg != null) {
  System.out.println("\nInitialization failed:\n" + msg);
  return;
}
System.out.println("\nTransformed");
System.out.println(MatrixHelper.toString(pls.transform(predictors)));
System.out.println("\nPredictions");
System.out.println(MatrixHelper.toString(pls.predict(predictors)));
System.out.println("\nLoadings");
System.out.println(MatrixHelper.toString(pls.getLoadings()));
```
