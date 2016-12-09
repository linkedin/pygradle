from sklearn.model_selection import train_test_split
from sklearn import datasets
from sklearn import svm


def main():
    iris = datasets.load_iris()

    X_train, X_test, y_train, y_test = train_test_split(
        iris.data, iris.target, test_size=0.4, random_state=0)

    clf = svm.SVC(kernel='linear', C=1).fit(X_train, y_train)
    print("Accuracy score on the IRIS dataset using a 60/40 split: {}"
          .format(clf.score(X_test, y_test)))

if __name__ == '__main__':
    print("This is not printed when run with PEX ...")
    main()
