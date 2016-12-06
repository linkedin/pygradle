import unittest
from sklearn import datasets


class Test(unittest.TestCase):

    def testIrisDataset(self):
        iris = datasets.load_iris()
        self.assertEqual(150,
                         len(iris.data),
                         "The dataset shall contain 150 instances")


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testIrisDataset']
    unittest.main()
