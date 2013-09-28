//
//  LicenceTableViewController.m
//  Salesman Companion
//
//  Created by Taylor McCord on 9/18/13.
//  Copyright (c) 2013 McCord Inc. All rights reserved.
//

#import "LicenceTableViewController.h"
#import "NSURLConnectionWithTag.h"

@interface LicenceTableViewController ()

@end

@implementation LicenceTableViewController

@synthesize licenses;

NSString *baseUrl = @"http://salesmanbuddytest1.elasticbeanstalk.com/v1/salesmanbuddy/";
NSString *licensesUrl = @"licenses";

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        self.title = NSLocalizedString(@"Customers", @"Customers");
        self.tabBarItem.image = [UIImage imageNamed:@"first"];
        self.licenses = [[NSMutableArray alloc] initWithCapacity:0];
        connectionNumber = [NSDecimalNumber one];
        typeMyItems = [NSNumber numberWithInt:1];
        authCall = nil;
    }
    return self;
}

- (void)viewDidLoad{
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)didReceiveMemoryWarning{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView{
    return 0;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    return licenses.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *CellIdentifier = @"Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    if(cell == NULL){
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    [[[self licenses] objectAtIndex:[indexPath row]]]
    [[cell textLabel] setText:[[self.licenses objectAtIndex:[indexPath row]] created]];
    
    return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     */
}

-(void)returnedLicenses:(NSArray *)newLicenses{
    self.licenses = [NSMutableArray arrayWithArray:newLicenses];
    [[self tableView] reloadData];
}


-(void)getMyItems:(NSInteger)userId{
    NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@%@", baseUrl, licensesUrl]];
    url = [url URLByAppendingQueryStringKey:@"userid" value:userId];
    [callQueue addObject:[[CallQueue alloc] initQueueItem:url type:typeMyItems body:nil delegate:self]];
    [self doFetchQueue];
}

// Queue stuff
-(void)doFetchQueue{
    if (authCall) {
        if (!authCall.alreadySent) {
            authCall.alreadySent = YES;
//            [self doRequest:authCall.fullUrl body:nil forType:authCall.type finalDelegate:authCall.delegate];
        }
    }else if (callQueue.count != 0) {
        for (CallQueue *cq in callQueue) {
            if (!cq.alreadySent) {
                NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:cq.fullUrl];
                if (cq.body == nil) {
                    
                }else{
                    NSError *error = nil;
                    NSData *postData = [NSJSONSerialization dataWithJSONObject:cq.body options:NSJSONWritingPrettyPrinted error:&error];
                    if (error) {
                        NSLog(@"error serializing body data: %@", error.localizedDescription);
                    }
                    [request setHTTPBody:postData];
                    [request setValue:[NSString stringWithFormat:@"%d", [postData length]] forHTTPHeaderField:@"Content-Length"];
                    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
                }
                [self doRequest:request forType:cq.type finalDelegate:cq.delegate];
            }
        }
    }
}

// Initialize Connection
-(void)doRequest:(NSURLRequest *)request forType:(NSNumber *)type finalDelegate:(id)delegate{
    [connections setObject:[[NSURLConnectionWithTag alloc]initWithRequest:request delegate:self startImmediately:YES typeTag:type uniqueTag:connectionNumber finalDelegate:delegate] forKey:connectionNumber];
    [connectionNumber decimalNumberByAdding:[NSDecimalNumber one]];
}

// Connection handling
-(void)connection:(NSURLConnectionWithTag *)connection didReceiveData:(NSData *)data{
    if ([dataFromConnectionByTag objectForKey:connection.uniqueTag] == nil) {
        NSMutableData *newData = [[NSMutableData alloc] initWithData:data];
        [dataFromConnectionByTag setObject:newData forKey:connection.uniqueTag];
        return;
    }else{
        [[dataFromConnectionByTag objectForKey:connection.uniqueTag] appendData:data];
    }
}

// TODO connection needs to store the final delegate******************************************************************************************************************************
-(void)connectionDidFinishLoading:(NSURLConnectionWithTag *)conn{
    if ([dataFromConnectionByTag objectForKey:conn.uniqueTag]) {
        if ([conn.typeTag isEqualToNumber:typeMyItems]) {// history, code, attributes, location
            if ([conn.finalDelegate respondsToSelector:@selector(returnedLicenses:)]) {
                NSError *e = nil;
                NSArray *jsonArray = [NSJSONSerialization JSONObjectWithData:[dataFromConnectionByTag objectForKey:conn.uniqueTag] options:NSJSONReadingMutableContainers error:&e];
                if (e != nil) {
                    NSLog(@"There was an error parsing all licenses data: %@", e.localizedDescription);
                }else{
                    NSLog(@"result sent to delegate");
                    [conn.finalDelegate returnedLicenses:jsonArray];
                }
            }
        }
    }else
        NSLog(@"couldnt find data for typeTag: %@", conn.typeTag);
    
    // clean up
    [dataFromConnectionByTag removeObjectForKey:conn.uniqueTag]; // after done using the data, remove it
    [connections removeObjectForKey:conn.uniqueTag];// remove the connection
}

-(void)connection:(NSURLConnectionWithTag *)conn didFailWithError:(NSError *)error{
    if ([conn.typeTag isEqualToNumber:typeMyItems]) {
        NSLog(@"getting licenses failed, error: %@", error.localizedDescription);
    }
    NSString *errorString = [NSString stringWithFormat:@"Fetch  failed for url: %@, error: %@", conn.originalRequest.URL.absoluteString, [error localizedDescription]];
    UIAlertView *av = [[UIAlertView alloc] initWithTitle:@"Error" message:errorString delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];
    [av show];
}

@end
