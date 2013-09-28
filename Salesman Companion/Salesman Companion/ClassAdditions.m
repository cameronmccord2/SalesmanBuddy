@implementation NSString (Additions)

- (NSURL *)URLByAppendingQueryStringKey:(NSString *)queryStringKey value:(NSString *)queryStringValue {
    if (![queryStringKey length]) {
        return [NSURL URLWithString:self];
    }
    NSString *queryString = [NSString stringWithFormat:@"%@=%@", queryStringKey, queryStringValue];
    
    NSString *URLString = [[NSString alloc] initWithFormat:@"%@%@%@", self,
                           [self rangeOfString:@"?"].length > 0 ? @"&" : @"?", [queryString stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    NSURL *theURL = [NSURL URLWithString:URLString];
    return theURL;
}

// Or:

- (NSString *)URLStringByAppendingQueryStringKey:(NSString *)queryStringKey value:(NSString *)queryStringValue {
    if (![queryStringKey length]) {
        return self;
    }
    
    NSString *queryString = [NSString stringWithFormat:@"%@=%@", queryStringKey, queryStringValue];
    
    return [NSString stringWithFormat:@"%@%@%@", self,
            [self rangeOfString:@"?"].length > 0 ? @"&" : @"?", [queryString stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
}

@end

@implementation NSURL (Additions)

- (NSURL *)URLByAppendingQueryStringKey:(NSString *)queryStringKey value:(NSString *)queryStringValue {
    if (![queryStringKey length]) {
        return self;
    }
    
    NSString *queryString = [NSString stringWithFormat:@"%@=%@", queryStringKey, queryStringValue];
    
    NSString *URLString = [[NSString alloc] initWithFormat:@"%@%@%@", [self absoluteString],
                           [self query] ? @"&" : @"?", [queryString stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    NSURL *theURL = [NSURL URLWithString:URLString];
    return theURL;
}

@end