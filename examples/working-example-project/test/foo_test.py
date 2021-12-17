import unittest

from src import foo


class MyTestCase(unittest.TestCase):
    def test_print_hello_world(self):
        self.assertEqual(foo.hello_world(), "hello world!")  # add assertion here


if __name__ == '__main__':
    unittest.main()
