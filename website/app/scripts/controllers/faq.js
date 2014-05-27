'use strict';

angular.module('salesmanBuddyApp')
  .controller('FaqCtrl', ['$scope', function ($scope) {
  	$scope.faqs = [
		{question:'This is a question?', answer:'This is an answer and it is longer than the question to simulate an actual faq entry. This is an answer and it is longer than the question to simulate an actual faq entry. This is an answer and it is longer than the question to simulate an actual faq entry.'},
		{question:'This is a question?', answer:'This is an answer and it is longer than the question to simulate an actual faq entry.'},
		{question:'This is a question?', answer:'This is an answer and it is longer than the question to simulate an actual faq entry.'},
		{question:'This is a question?', answer:'This is an answer and it is longer than the question to simulate an actual faq entry.'},
		{question:'This is a question?', answer:'This is an answer and it is longer than the question to simulate an actual faq entry.'},
		{question:'This is a question?', answer:'This is an answer and it is longer than the question to simulate an actual faq entry.'},
		{question:'This is a question?', answer:'This is an answer and it is longer than the question to simulate an actual faq entry.'},
		{question:'This is a question?', answer:'This is an answer and it is longer than the question to simulate an actual faq entry.'}
	];
}]);
