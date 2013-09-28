//
//  NSURLConnectionWithTag.h
//  VP+ Book
//
//  Created by Taylor McCord on 8/19/13.
//  Copyright (c) 2013 Cameron McCord. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSURLConnectionWithTag : NSURLConnection{

}

@property (nonatomic, strong)NSNumber *typeTag;
@property (nonatomic, strong)NSDecimalNumber *uniqueTag;
@property (nonatomic, strong)id finalDelegate;

-(id)initWithRequest:(NSURLRequest *)request delegate:(id)delegate startImmediately:(BOOL)startImmediately typeTag:(NSNumber *)newTag uniqueTag:(NSDecimalNumber *)newUniqueTag finalDelegate:(id)newFinalDelegate;

@end
